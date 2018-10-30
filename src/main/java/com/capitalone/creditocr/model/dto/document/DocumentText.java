package com.capitalone.creditocr.model.dto.document;

import com.capitalone.creditocr.util.Simhash;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public final class DocumentText {

    private int id;
    private String documentText;
    private BigInteger fingerprint;
    private int imageId;


    private DocumentText(String documentText, BigInteger fingerprint, int imageId) {
        this.documentText = documentText;
        this.fingerprint = fingerprint;
        this.imageId = imageId;
    }

    public DocumentText(String documentText, int imageId) {
        this(documentText, Simhash.hash(documentText), imageId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocumentText() {
        return documentText;
    }

    public void setDocumentText(String documentText) {
        this.documentText = documentText;
    }

    public byte[] getFingerprint() {
        return fingerprint.toByteArray();
    }

    public void setFingerprint(byte[] fingerprint) {
        this.fingerprint = new BigInteger(fingerprint);
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentText that = (DocumentText) o;
        return getId() == that.getId() &&
                getImageId() == that.getImageId() &&
                Objects.equals(getDocumentText(), that.getDocumentText()) &&
                Arrays.equals(getFingerprint(), that.getFingerprint());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getDocumentText(), getImageId());
        result = 31 * result + Arrays.hashCode(getFingerprint());
        return result;
    }

    @Override
    public String toString() {
        return "DocumentText{" +
                "id=" + id +
                ", documentText='" + documentText + '\'' +
                ", fingerprint=" + fingerprint.toString(16) +
                ", imageId=" + imageId +
                '}';
    }
}
