package com.capitalone.creditocr.view;

import com.capitalone.creditocr.model.dto.document.Document;

@SuppressWarnings("unused") // Getter methods are used internally by Jackson when converting to JSON.
public class DocumentResponse {

    private int id;
    private long accountNumber;
    // address
    private String ssn;
    private long dateOfBirth;
    private long letterDate;
    private long postmarkDate;
    private int numSimilarDocuments;
    private int numPages;
    private boolean hasEnvelope;
    private String disputeQueue;

    public DocumentResponse(Document document) {
        id = document.getId();

        if (document.getAccountNumber() != null) {
            accountNumber = document.getAccountNumber();
        }

        ssn = document.getSsn();

        if (document.getDateOfBirth() != null) {
            dateOfBirth = document.getDateOfBirth().getEpochSecond();
        }

        if (document.getLetterDate() != null) {
            letterDate = document.getLetterDate().getEpochSecond();
        }

        if (document.getPostmarkDate() != null) {
            postmarkDate = document.getPostmarkDate().getEpochSecond();
        }

        numSimilarDocuments = document.getNumSimilarDocuments();

        disputeQueue = document.getQueue();
    }


    public int getId() {
        return id;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getSsn() {
        return ssn;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public long getLetterDate() {
        return letterDate;
    }

    public long getPostmarkDate() {
        return postmarkDate;
    }

    public int getNumSimilarDocuments() {
        return numSimilarDocuments;
    }

    public int getNumPages() {
        return numPages;
    }

    public boolean isHasEnvelope() {
        return hasEnvelope;
    }

    public String getDisputeQueue() {
        return disputeQueue;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public void setHasEnvelope(boolean hasEnvelope) {
        this.hasEnvelope = hasEnvelope;
    }

    @Override
    public String toString() {
        return "DocumentResponse{" +
                "id=" + id +
                ", accountNumber=" + accountNumber +
                ", ssn='" + ssn + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", letterDate=" + letterDate +
                ", postmarkDate=" + postmarkDate +
                ", numSimilarDocuments=" + numSimilarDocuments +
                ", numPages=" + numPages +
                ", hasEnvelope=" + hasEnvelope +
                ", disputeQueue='" + disputeQueue + '\'' +
                '}';
    }
}
