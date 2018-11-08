package com.capitalone.creditocr.model.dto.job;


import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * DTO for jobs table
 */
public class ProcessingJob {

    private int id;
    private Instant creationTime;
    private @Nullable Integer imageFk;
    private @Nullable Integer documentFk;
    private JobType jobType;

    private ProcessingJob(Instant creationTime, @Nullable Integer imageFk, @Nullable Integer documentFk, JobType jobType) {
        this.creationTime = creationTime;
        this.imageFk = imageFk;
        this.documentFk = documentFk;
        this.jobType = jobType;
    }

    public static ProcessingJob imageJob(Instant time, int imageId) {
        return new ProcessingJob(time, imageId, null, JobType.IMAGE);
    }

    public static ProcessingJob documentJob(Instant time, int documentId) {
        return new ProcessingJob(time, null, documentId, JobType.FINGERPRINT);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Nullable
    public Integer getImageFk() {
        return imageFk;
    }

    @Nullable
    public Integer getDocumentFk() {
        return documentFk;
    }

    public void setDocumentFk(@Nullable Integer documentFk) {
        this.documentFk = documentFk;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    @Override
    public String toString() {
        return "ProcessingJob{" +
                "id=" + id +
                ", creationTime=" + creationTime +
                ", imageFk=" + imageFk +
                ", documentFk=" + documentFk +
                ", jobType=" + jobType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingJob job = (ProcessingJob) o;
        return getId() == job.getId() &&
                Objects.equals(getCreationTime(), job.getCreationTime()) &&
                Objects.equals(getImageFk(), job.getImageFk()) &&
                Objects.equals(getDocumentFk(), job.getDocumentFk()) &&
                getJobType() == job.getJobType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreationTime(), getImageFk(), getDocumentFk(), getJobType());
    }
}
