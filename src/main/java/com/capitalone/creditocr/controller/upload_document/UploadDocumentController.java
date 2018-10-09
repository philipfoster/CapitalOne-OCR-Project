package com.capitalone.creditocr.controller.upload_document;


import com.capitalone.creditocr.controller.exception.InternalServerErrorException;
import com.capitalone.creditocr.controller.exception.UnsupportedFileTypeException;
import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dto.ImageType;
import com.capitalone.creditocr.model.dto.document.DocumentDto;
import com.capitalone.creditocr.model.dto.document_image.DocumentImageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * This class is responsible for handling ingest requests.
 */
@RestController
public class UploadDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(UploadDocumentController.class);
    private static final String CONTENT_TYPE_ZIP = "application/zip";

    private final DocumentImageDao imageDao;
    private final DocumentDao documentDao;

    @Autowired
    public UploadDocumentController(DocumentImageDao imageDao, DocumentDao documentDao) {
        this.imageDao = imageDao;
        this.documentDao = documentDao;
    }

    /**
     * Load the uploaded document into the database, and create a job intent.
     *
     * @param file The file from the request
     * @throws UnsupportedFileTypeException if the client attempts to upload a document with an invalid file type
     * @throws InternalServerErrorException if an unrecoverable error occurs.
     */
    @PostMapping("/documents")
    @Transactional(rollbackFor = Exception.class)
    public void processRequest(@RequestParam("file") MultipartFile file) {

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

        // Add a blank row into the db as a placeholder. This also lets us get an auto-generated primary key for
        // referencing by other tables.
        DocumentDto document = DocumentDto.builder()
                .build();
        documentDao.createDocument(document);


        // Add the file to the database
        if (CONTENT_TYPE_ZIP.equals(contentType)) {
            // TODO: Handle this case
            // We will need to extract the images before uploading them

            return;
        }

        storeImage(fileContent, ImageType.fromContentType(contentType), 0, document);
    }

    private void storeImage(byte[] fileContent, ImageType contentType, int pageNum, DocumentDto document) {
        DocumentImageDto documentImage = DocumentImageDto.builder()
                .setIsEnvelope(pageNum > 0)
                .setPageNumber(pageNum)
                .setImageType(contentType)
                .setFileData(fileContent)
                .setDocumentId(document.getId())
                .build();

        imageDao.addNewImage(documentImage);

        // TODO: Generate job intent in the jobs table
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
