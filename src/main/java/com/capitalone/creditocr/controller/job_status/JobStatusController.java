package com.capitalone.creditocr.controller.job_status;

import com.capitalone.creditocr.controller.exception.FileNotFoundException;
import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

// Written by Andrew Mollenkamp

@RestController
public class JobStatusController {

    private final JobDao jobDao;
    private final DocumentDao documentDao;

    @Autowired
    public JobStatusController(JobDao dao, DocumentDao documentDao) {
        this.jobDao = dao;
        this.documentDao = documentDao;
    }

    /**
     * Method to determine the current status of a job in json.
     * @return 200 if job is found, 404 if job is not found.
     */
    @GetMapping("/documents/jobs/{id}")
    @Transactional(rollbackFor = Exception.class)
    public JobResponse getJobStatus(@PathVariable("id") int id) {
        Optional<ProcessingJob> job = jobDao.getJobByID(id);


        if (job.isEmpty()) {
            throw new FileNotFoundException("Job with id " + id + " not found in database.");
        }

        ProcessingJob firstJob = job.get();
        JobResponse jobResponse = new JobResponse();

        jobResponse.setJobID(firstJob.getId());
        jobResponse.setStatus(jobDao.getJobStatus(id));
        jobResponse.setDocument(documentDao.getDocumentIDbyJob(id));

        return jobResponse;
    }
}