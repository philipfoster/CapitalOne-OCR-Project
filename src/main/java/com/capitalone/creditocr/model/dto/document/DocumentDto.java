package com.capitalone.creditocr.model.dto.document;

import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Objects;

public final class DocumentDto {


    private int id = -1;

    private long accountNumber;

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

    /**
     * A foreign key to get document text
     */
    private int textId;


    public DocumentDto(long accountNumber, @Nullable String ssn, @Nullable Instant letterDate,
                       @Nullable Instant dateOfBirth, @Nullable Instant postmarkDate, int numSimilarDocuments,
                       int addressId, int textId) {
        this.accountNumber = accountNumber;
        this.ssn = ssn;
        this.letterDate = letterDate;
        this.dateOfBirth = dateOfBirth;
        this.postmarkDate = postmarkDate;
        this.numSimilarDocuments = numSimilarDocuments;
        this.addressId = addressId;
        this.textId = textId;
    }

    public static DocumentDtoBuilder builder() {
        return new DocumentDtoBuilder();
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getId() {
        return id;
    }

    public long getAccountNumber() {
        return accountNumber;
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

    public int getAddressId() {
        return addressId;
    }

    public int getTextId() {
        return textId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentDto that = (DocumentDto) o;
        return getAccountNumber() == that.getAccountNumber() &&
                getNumSimilarDocuments() == that.getNumSimilarDocuments() &&
                getAddressId() == that.getAddressId() &&
                getTextId() == that.getTextId() &&
                Objects.equals(getSsn(), that.getSsn()) &&
                Objects.equals(getLetterDate(), that.getLetterDate()) &&
                Objects.equals(getDateOfBirth(), that.getDateOfBirth()) &&
                Objects.equals(getPostmarkDate(), that.getPostmarkDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountNumber(), getSsn(), getLetterDate(), getDateOfBirth(), getPostmarkDate(),
                getNumSimilarDocuments(), getAddressId(), getTextId());
    }


    @Override
    public String toString() {
        return "DocumentDto{" +
                "accountNumber=" + accountNumber +
                ", ssn='" + ssn + '\'' +
                ", letterDate=" + letterDate +
                ", dateOfBirth=" + dateOfBirth +
                ", postmarkDate=" + postmarkDate +
                ", numSimilarDocuments=" + numSimilarDocuments +
                ", addressId=" + addressId +
                ", textId=" + textId +
                '}';
    }
}
