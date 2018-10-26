package com.capitalone.creditocr.model.dao;

import com.capitalone.creditocr.controller.job_status.JobStatusEnum;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * Get all available jobs
     * @param pageSize The number of elements to return
     * @param pageNum The page number to start at.
     */
    List<ProcessingJob> getAvailableJobs(int pageSize, int pageNum);

    /**
     * Take the next job out of the job queue, and return it for processing (if one exists)
     * @param serverId The ID of the server accepting the job
     * @return The job that was accepted
     */
    Optional<ProcessingJob> acceptNextJob(UUID serverId);

    void completeJob(ProcessingJob job);

    Optional<ProcessingJob> getJobByID(int id);

    JobStatusEnum getJobStatus(int id);
}
