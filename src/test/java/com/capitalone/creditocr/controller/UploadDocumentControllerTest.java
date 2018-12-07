package com.capitalone.creditocr.controller;

import com.capitalone.creditocr.controller.upload_document.UploadDocumentController;
import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.model.dto.document_image.DocumentImage;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import com.capitalone.creditocr.view.JobListResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = UploadDocumentController.class, secure = false)
public class UploadDocumentControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockBean private DocumentImageDao imageDao;
    @MockBean private JobDao jobDao;
    @MockBean private DocumentDao documentDao;

    /**
     * Test the case where the user uploads an illegal file type.
     * The controller should return with a 415 status code.
     */
    @Test
    public void testIllegalFileType() throws Exception {

        MockMultipartFile mmf = new MockMultipartFile("file", "file", "image/gif",
                getClass().getResourceAsStream("/UploadDocumentTest/illegal_format.gif"));

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/documents").file(mmf)
        )
                .andExpect(status().is(415));
    }

    /**
     * Test the simple case where a single file was uploaded.
     * The controller should return JSON with information about the created job, and
     */
    @Test
    public void testSingleFileUpload() throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", "file", "image/png",
                getClass().getResourceAsStream("/UploadDocumentTest/single_file.png"));

        AtomicInteger docCount = new AtomicInteger(0);
        AtomicInteger jobCount = new AtomicInteger(0);
        AtomicInteger imageCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId(docCount.incrementAndGet());
            return null; // void method, so return null
        }).when(documentDao).createDocument(any(Document.class));

        doAnswer(invocation -> {
            ProcessingJob job = invocation.getArgument(0);
            job.setId(jobCount.incrementAndGet());
            return null;
        }).when(jobDao).createImageProcessingJob(any(ProcessingJob.class));

        doAnswer(invocation -> {
            DocumentImage image = invocation.getArgument(0);
            image.setId(imageCount.incrementAndGet());
            return null;
        }).when(imageDao).addNewImage(any(DocumentImage.class));


        mockMvc.perform(MockMvcRequestBuilders.multipart("/documents").file(file))
                .andDo(print())
                .andExpect(status().is(202))
                .andExpect(content().json("[{\"documentId\":1,\"jobIds\":[1]}]"));
    }

    @Test
    public void testMultiImageUpload() throws Exception {

        AtomicInteger docCount = new AtomicInteger(0);
        AtomicInteger jobCount = new AtomicInteger(0);
        AtomicInteger imageCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId(docCount.incrementAndGet());
            return null; // void method, so return null
        }).when(documentDao).createDocument(any(Document.class));

        doAnswer(invocation -> {
            ProcessingJob job = invocation.getArgument(0);
            job.setId(jobCount.incrementAndGet());
            return null;
        }).when(jobDao).createImageProcessingJob(any(ProcessingJob.class));

        doAnswer(invocation -> {
            DocumentImage image = invocation.getArgument(0);
            image.setId(imageCount.incrementAndGet());
            return null;
        }).when(imageDao).addNewImage(any(DocumentImage.class));


        MockMultipartFile file = new MockMultipartFile("file", "file", "application/zip",
                getClass().getResourceAsStream("/UploadDocumentTest/single_files_test.zip"));


        mockMvc.perform(MockMvcRequestBuilders.multipart("/documents").file(file))
                .andDo(print())
                .andExpect(status().is(202))
                .andExpect(content().json("[{\"documentId\":1,\"jobIds\":[1]},{\"documentId\":2,\"jobIds\":[2]},{\"documentId\":3,\"jobIds\":[3]}]"));
    }

    @Test
    public void testMultiPageDocumentUpload() throws Exception {

        AtomicInteger docCount = new AtomicInteger(0);
        AtomicInteger jobCount = new AtomicInteger(0);
        AtomicInteger imageCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId(docCount.incrementAndGet());
            return null; // void method, so return null
        }).when(documentDao).createDocument(any(Document.class));

        doAnswer(invocation -> {
            ProcessingJob job = invocation.getArgument(0);
            job.setId(jobCount.incrementAndGet());
            return null;
        }).when(jobDao).createImageProcessingJob(any(ProcessingJob.class));

        doAnswer(invocation -> {
            DocumentImage image = invocation.getArgument(0);
            image.setId(imageCount.incrementAndGet());
            return null;
        }).when(imageDao).addNewImage(any(DocumentImage.class));

        MockMultipartFile file = new MockMultipartFile("file", "file", "application/zip",
                getClass().getResourceAsStream("/UploadDocumentTest/everything_test.zip"));

        // We can't do a simple comparison like the previous tests, because the Files.walk() method will
        // A file tree is walked depth first, but you cannot make any assumptions about the iteration order that subdirectories are visited.
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/documents").file(file))
                .andDo(print())
                .andExpect(status().is(202))
                .andReturn();

        // Deserialize json into a list of objects
        String json = result.getResponse().getContentAsString();
        List<JobListResponse> responses = new Gson().fromJson(json, new TypeToken<List<JobListResponse>>() {}.getType());

        // Make sure there are 3 single-page documents
        long numSinglePages = responses.stream().filter(obj -> obj.getJobIds().size() == 1).count();
        assertEquals(3, numSinglePages);

        // Make sure there is 1 2-page document
        List<JobListResponse> twoPageDocs= responses.stream().filter(obj -> obj.getJobIds().size() == 2).collect(Collectors.toList());
        assertEquals(1, twoPageDocs.size());

        // Make sure there is 1 6-page document
        List<JobListResponse> sixPageDocs = responses.stream().filter(obj -> obj.getJobIds().size() == 6).collect(Collectors.toList());
        assertEquals(1, sixPageDocs.size());

    }

}