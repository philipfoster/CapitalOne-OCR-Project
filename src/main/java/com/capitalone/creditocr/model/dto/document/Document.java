package com.capitalone.creditocr.model.dto.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public final class Document {

    private static final Logger logger = LoggerFactory.getLogger(Document.class);

    private int id = -1;

    @Nullable
    private Long accountNumber;

    @Nullable
    private String ssn;

    @Nullable
    private Instant letterDate;

    @Nullable
    private Instant dateOfBirth;

    @Nullable
    private Instant postmarkDate;

    private int numSimilarDocuments;

    private int addressId;

    @Nullable
    private byte[] fingerprint;

    Document(long accountNumber, @Nullable String ssn, @Nullable Instant letterDate,
             @Nullable Instant dateOfBirth, @Nullable Instant postmarkDate, int numSimilarDocuments,
             int addressId, @Nullable byte[] fingerprint) {
        this.accountNumber = accountNumber;
        this.ssn = ssn;
        this.letterDate = letterDate;
        this.dateOfBirth = dateOfBirth;
        this.postmarkDate = postmarkDate;
        this.numSimilarDocuments = numSimilarDocuments;
        this.addressId = addressId;
        this.fingerprint = fingerprint;
    }

    public static DocumentBuilder builder() {
        return new DocumentBuilder();
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    @Nullable
    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(@Nullable Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Nullable
    public String getSsn() {
        return ssn;
    }

    @Nullable
    public Instant getLetterDate() {
        return letterDate;
    }

    @Nullable
    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    @Nullable
    public Instant getPostmarkDate() {
        return postmarkDate;
    }

    public int getNumSimilarDocuments() {
        return numSimilarDocuments;
    }

    public void setNumSimilarDocuments(int numSimilarDocuments) {
        this.numSimilarDocuments = numSimilarDocuments;
    }

    public int getAddressId() {
        return addressId;
    }


    public void setSsn(@Nullable String ssn) {
        this.ssn = ssn;
    }

    public void setLetterDate(@Nullable Instant letterDate) {
        this.letterDate = letterDate;
    }

    public void setPostmarkDate(@Nullable Instant postmarkDate) {
        this.postmarkDate = postmarkDate;
    }

    @Nullable
    public byte[] getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(@Nullable byte[] fingerprint) {

        this.fingerprint = fingerprint;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return getId() == document.getId() &&
                getNumSimilarDocuments() == document.getNumSimilarDocuments() &&
                getAddressId() == document.getAddressId() &&
                Objects.equals(getAccountNumber(), document.getAccountNumber()) &&
                Objects.equals(getSsn(), document.getSsn()) &&
                Objects.equals(getLetterDate(), document.getLetterDate()) &&
                Objects.equals(getDateOfBirth(), document.getDateOfBirth()) &&
                Objects.equals(getPostmarkDate(), document.getPostmarkDate()) &&
                Arrays.equals(getFingerprint(), document.getFingerprint());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getAccountNumber(), getSsn(), getLetterDate(), getDateOfBirth(), getPostmarkDate(), getNumSimilarDocuments(), getAddressId());
        result = 31 * result + Arrays.hashCode(getFingerprint());
        return result;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", accountNumber=" + accountNumber +
                ", ssn='" + ssn + '\'' +
                ", letterDate=" + letterDate +
                ", dateOfBirth=" + dateOfBirth +
                ", postmarkDate=" + postmarkDate +
                ", numSimilarDocuments=" + numSimilarDocuments +
                ", addressId=" + addressId +
                ", fingerprint=" + Arrays.toString(fingerprint) +
                '}';
    }
}
