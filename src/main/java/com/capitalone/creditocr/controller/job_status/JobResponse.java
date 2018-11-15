package com.capitalone.creditocr.controller.job_status;

import java.util.Objects;

public class JobResponse {

    private int jobID;
    private JobStatusEnum status;
    private Integer document = null;

    public JobResponse() {

    }

    public JobResponse(int jobID, JobStatusEnum status, int document) {
        this.jobID = jobID;
        this.status = status;
        this.document = document;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public JobStatusEnum getStatus() {
        return status;
    }

    public void setStatus(JobStatusEnum status) {
        this.status = status;
    }

    public int getDocument() {
        return document;
    }

    public void setDocument(int document) {
        this.document = document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobResponse that = (JobResponse) o;
        return jobID == that.jobID &&
                document == that.document &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobID, status, document);
    }

    @Override
    public String toString() {
        return "JobResponse{" +
                "jobID=" + jobID +
                ", status=" + status +
                ", document=" + document +
                '}';
    }
}


