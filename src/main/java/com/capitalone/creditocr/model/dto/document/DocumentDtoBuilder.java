package com.capitalone.creditocr.model.dto.document;

import java.time.Instant;

public class DocumentDtoBuilder {
    private long accountNumber;
    private String ssn;
    private Instant letterDate;
    private Instant dateOfBirth;
    private Instant postmarkDate;
    private int numSimilarDocuments;
    private int addressId;
    private int textId;

    public DocumentDtoBuilder setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public DocumentDtoBuilder setSsn(String ssn) {
        this.ssn = ssn;
        return this;
    }

    public DocumentDtoBuilder setLetterDate(Instant letterDate) {
        this.letterDate = letterDate;
        return this;
    }

    public DocumentDtoBuilder setDateOfBirth(Instant dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public DocumentDtoBuilder setPostmarkDate(Instant postmarkDate) {
        this.postmarkDate = postmarkDate;
        return this;
    }

    public DocumentDtoBuilder setNumSimilarDocuments(int numSimilarDocuments) {
        this.numSimilarDocuments = numSimilarDocuments;
        return this;
    }

    public DocumentDtoBuilder setAddressId(int addressId) {
        this.addressId = addressId;
        return this;
    }

    public DocumentDtoBuilder setTextId(int textId) {
        this.textId = textId;
        return this;
    }

    public DocumentDto build() {
        return new DocumentDto(accountNumber, ssn, letterDate, dateOfBirth, postmarkDate, numSimilarDocuments, addressId, textId);
    }
}