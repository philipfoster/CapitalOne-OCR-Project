package com.capitalone.creditocr.model.dto.document;

import java.time.Instant;

public class DocumentBuilder {
    private long accountNumber;
    private String ssn;
    private Instant letterDate;
    private Instant dateOfBirth;
    private Instant postmarkDate;
    private int numSimilarDocuments;
    private int addressId;
    private byte[] fingerprint;

    public DocumentBuilder setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public DocumentBuilder setSsn(String ssn) {
        this.ssn = ssn;
        return this;
    }

    public DocumentBuilder setLetterDate(Instant letterDate) {
        this.letterDate = letterDate;
        return this;
    }

    public DocumentBuilder setDateOfBirth(Instant dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public DocumentBuilder setPostmarkDate(Instant postmarkDate) {
        this.postmarkDate = postmarkDate;
        return this;
    }

    public DocumentBuilder setNumSimilarDocuments(int numSimilarDocuments) {
        this.numSimilarDocuments = numSimilarDocuments;
        return this;
    }

    public DocumentBuilder setAddressId(int addressId) {
        this.addressId = addressId;
        return this;
    }

    public DocumentBuilder setFingerprint(byte[] fingerprint) {
        this.fingerprint = fingerprint;
        return this;
    }

    public Document build() {
        return new Document(accountNumber, ssn, letterDate, dateOfBirth, postmarkDate, numSimilarDocuments, addressId, fingerprint);
    }
}