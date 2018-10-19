package com.capitalone.creditocr.model.dao;

import com.capitalone.creditocr.model.dto.job.ProcessingJob;

import java.util.Optional;

/**
 * DAO for {@link ProcessingJob}
 */
public interface JobDao {

    /**
     * Insert a job intent into the jobs table. The {@link ProcessingJob#id} field will
     * be updated with the auto-generated primary key after insertion.
     */
    void createJob(ProcessingJob job);

    /**
     * Take the next job out of the job queue, and return it for processing (if one exists)
     * @param serverId The ID of the server accepting the job
     * @return The job that was accepted
     */
    Optional<ProcessingJob> acceptNextJob(String serverId);

}
