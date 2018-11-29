package com.capitalone.creditocr.view;

import java.util.List;


public class JobListResponse {

    private int documentId;
    /**
     * A list of jobs for each page.
     */
    private List<Integer> jobIds;

    public JobListResponse(int documentId, List<Integer> jobIds) {
        this.documentId = documentId;
        this.jobIds = jobIds;
    }

    public int getDocumentId() {
        return documentId;
    }

    public List<Integer> getJobIds() {
        return jobIds;
    }
}
