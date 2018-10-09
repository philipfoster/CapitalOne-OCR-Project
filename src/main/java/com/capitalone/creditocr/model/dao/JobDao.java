package com.capitalone.creditocr.model.dao;

import com.capitalone.creditocr.model.dto.job.ProcessingJob;

/**
 * DAO for {@link ProcessingJob}
 */
public interface JobDao {

    /**
     * Insert a job intent into the jobs table. The {@link ProcessingJob#id} field will
     * be updated with the auto-generated primary key after insertion.
     */
    void createJob(ProcessingJob job);

}
