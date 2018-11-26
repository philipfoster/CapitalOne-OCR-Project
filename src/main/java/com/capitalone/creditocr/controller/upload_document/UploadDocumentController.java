package com.capitalone.creditocr.controller.upload_document;


import com.capitalone.creditocr.controller.exception.InternalServerErrorException;
import com.capitalone.creditocr.controller.exception.UnsupportedFileTypeException;
import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.ImageType;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.model.dto.document_image.DocumentImage;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import com.capitalone.creditocr.util.PdfUtil;
import com.capitalone.creditocr.util.UnzipUtil;
import com.capitalone.creditocr.view.DocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for handling ingest requests.
 */
@RestController
public class UploadDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(UploadDocumentController.class);
    private static final String CONTENT_TYPE_ZIP = "application/zip";
    private static final int PAGE_NUM_ENVELOPE = -1;
    private static final String FILENAME_ENVELOPE = "envelope";

    private final DocumentImageDao imageDao;
    private final DocumentDao documentDao;
    private final JobDao jobDao;

    @Autowired
    public UploadDocumentController(DocumentImageDao imageDao, DocumentDao documentDao, JobDao jobDao) {
        this.imageDao = imageDao;
        this.documentDao = documentDao;
        this.jobDao = jobDao;
    }

    /**
     * Load the uploaded document into the database, and create a job intent. Upon successfully completing the
     * operation, a 202 status code will be returned instead of the normal 200.
     *
     * @param file The file from the request
     * @throws UnsupportedFileTypeException if the client attempts to upload a document with an invalid file type
     * @throws InternalServerErrorException if an unrecoverable error occurs.
//     */
    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional(rollbackFor = Exception.class)
    public List<DocumentResponse> processRequest(@RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        // Validate that Content-Type header is acceptable. If not, throw an exception.
        checkContentType(contentType);

        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            logger.error("Could not copy image content from request", e);
            throw new InternalServerErrorException("Could not copy image content from request", e);
        }

        logger.debug("Processing file. size = %d bytes, type = %s", fileContent.length, contentType);

        // Add the file to the database
        if (CONTENT_TYPE_ZIP.equals(contentType)) {
            return processZippedInput(fileContent);

        } else {
            // Add a blank row into the db as a placeholder. This also lets us get an auto-generated primary key for
            // referencing by other tables.
            Document document = makeDocumentEntry();

            List<Integer> jobIds;
            try {
                jobIds = storeImage(fileContent, ImageType.fromContentType(contentType), 0, document);
            } catch (IOException e) {
                logger.error("Could not store image", e);
                throw new InternalServerErrorException("Could not store image", e);
            }
            DocumentResponse response = new DocumentResponse(document.getId(), jobIds);
            return Collections.singletonList(response);
        }
    }


    private List<DocumentResponse> processZippedInput(byte[] fileContent) {
        List<DocumentResponse> responses = new ArrayList<>();
        Path workDir = null;
        try {
            // Extract the zipped contents and write them to a temp directory
            workDir = Files.createTempDirectory("ingest");
            Path unzipped = UnzipUtil.unzip(workDir.toFile(), fileContent).toPath();
            logger.info("path = " + workDir.toAbsolutePath());
            // Java streams seem to be a more elegant solution, since it top level files are treated as
            // different documents, while items grouped inside a folder are considered to be different parts of the same document.
            // This makes a simple recursive solution tricky, as it requires separate logic to detect if this is a top-level file,
            // as opposed to a grouped file. The groupingBy collector makes this easy.
            var entries = Files.walk(unzipped, 2)
                    .filter(path -> path.compareTo(unzipped) > 0)
                    .collect(Collectors.groupingBy(Path::getParent)).entrySet();

            for (var entry : entries) {
                Path parent = entry.getKey();
                List<Path> group = entry.getValue();
                if (parent.equals(unzipped)) {
                    // Top-level files. Treat as unique.
                    responses.addAll(storeIndividualFiles(group));
                } else {
                    // Grouped in directory. Treat as group of documents
                    try {
                        responses.add(storeGroupedFiles(group));
                    } catch (IOException e) {
                        throw new InternalServerErrorException("Could not read file", e);
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error occurred while writing files to disk.", e);
            throw new InternalServerErrorException("Could not write files to disk", e);
        } finally {
            if (workDir != null) {
                try {
                    // Files.delete will just throw DirectoryNotEmpty exception, so use a spring utility.
                    FileSystemUtils.deleteRecursively(workDir);
                } catch (IOException e) {
                    logger.error("Could not delete temp directory", e);
                }
            }
        }
        return responses;
    }

    /**
     * Handle the files in the root of the uploaded .zip directory.
     */
    @Transactional
    List<DocumentResponse> storeIndividualFiles(List<Path> files) throws IOException {
        List<DocumentResponse> list = new ArrayList<>();
        for (Path path : files) {
            if (path.toFile().isDirectory()) {
                // Don't try to ingest a directory. This will be handled by a later iteration
                continue;
            }
            Document document = makeDocumentEntry();

            byte[] content;
            try {
                content = Files.readAllBytes(path);
            } catch (IOException e) {
                // Re-throw with an unchecked exception, since we can't throw
                // a checked exception from here
                throw new InternalServerErrorException("Could not read file from disk", e);
            }
            List<Integer> jobIds = storeImage(content, ImageType.PNG, 0, document);
            DocumentResponse resp = new DocumentResponse(document.getId(), jobIds);

            ProcessingJob documentJob = ProcessingJob.documentJob(Instant.now(), document.getId());
            jobDao.createDocumentProcessingJob(documentJob, jobIds);

            list.add(resp);
        }

        return list;
    }

    private Document makeDocumentEntry() {
        Document document = Document.builder().build();
        documentDao.createDocument(document);
        return document;
    }

    private DocumentResponse storeGroupedFiles(List<Path> files) throws IOException {
        Document document = makeDocumentEntry();
        int pageOffset = 0;
        List<Integer> jobIds = new ArrayList<>();
        for (Path file : files) {
            if (file.toFile().isDirectory()) { continue; }
            String filename = file.toFile().getName().split("\\.")[0].toLowerCase();

            int pageNum;
            byte[] bytes = Files.readAllBytes(file);
            if (FILENAME_ENVELOPE.equals(filename)) {
                pageNum = PAGE_NUM_ENVELOPE;
                storeImage(bytes, ImageType.fromPath(file), pageNum, document);
            } else {
                if (filename.matches("[0-9]+")) {
                    pageNum = Integer.valueOf(filename);
                    List<Integer> pageJobs = storeImage(bytes, ImageType.fromPath(file), pageNum, document);
                    pageOffset += pageJobs.size()-1;
                    jobIds.addAll(pageJobs);
                } else {
                    // TODO: Find better solution than skipping the page. Maybe try to infer a page number somehow??
                    logger.error("Could not get page number for file " + file.getFileName());
                }
            }
        }

        ProcessingJob job = ProcessingJob.documentJob(Instant.now(), document.getId());
        jobDao.createDocumentProcessingJob(job, jobIds);

        return new DocumentResponse(document.getId(), jobIds);
    }

    /**
     * Store an image into the db, and create a job intent to process it.
     * @param fileContent the image to store
     * @param contentType the image's type
     * @param pageNum the page number for the image
     * @param document the document to associate the image with
     * @return the job ids generated for the processing request.
     */
    private List<Integer> storeImage(byte[] fileContent, @NonNull ImageType contentType, int pageNum, Document document) throws IOException {

        if (PdfUtil.isPdf(fileContent)) {
            return storePdf(fileContent, pageNum, document);
        }

        // Save image to DB
        DocumentImage documentImage = DocumentImage.builder()
                .setIsEnvelope(pageNum < 0)
                .setPageNumber(pageNum)
                .setImageType(contentType)
                .setFileData(fileContent)
                .setDocumentId(document.getId())
                .build();

        imageDao.addNewImage(documentImage);

        // Create processing job intent
        ProcessingJob intent = ProcessingJob.imageJob(Instant.now(), documentImage.getId());
        jobDao.createImageProcessingJob(intent);

        return Collections.singletonList(intent.getId());
    }

    private List<Integer> storePdf(byte[] fileContent, int pageNum, Document document) throws IOException {
        List<byte[]> pngs = PdfUtil.pdf2png(fileContent);
        List<Integer> ret = new ArrayList<>();

        for (int i = 0; i < pngs.size(); i++) {
            byte[] png = pngs.get(i);

            DocumentImage image = DocumentImage.builder()
                    .setPageNumber(pageNum+i)
                    .setIsEnvelope(false) // Need some way to detect this...
                    .setImageType(ImageType.PNG)
                    .setFileData(png)
                    .setDocumentId(document.getId())
                    .build();

            imageDao.addNewImage(image);
            var intent = ProcessingJob.imageJob(Instant.now(), image.getId());
            jobDao.createImageProcessingJob(intent);

            ret.add(intent.getId());
        }

        return ret;
    }

    /**
     * Check if the supplied Content-Type header is valid.
     * If the supplied type is not valid, a {@link UnsupportedFileTypeException} will be thrown.
     */
    private void checkContentType(@Nullable String suppliedType) throws UnsupportedFileTypeException {
        String[] acceptedTypes = new String[] {
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/tiff",
                "application/pdf",
                "application/zip"
        };

        boolean found = false;
        for (String type : acceptedTypes) {
            if (type.equals(suppliedType)) {
                found = true;
            }
        }

        if (!found) {
            throw new UnsupportedFileTypeException(acceptedTypes, suppliedType);
        }
    }

}
