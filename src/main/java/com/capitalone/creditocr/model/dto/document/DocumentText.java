package com.capitalone.creditocr.model.dto.document;

import java.util.Objects;

public final class DocumentText {

    private int id;
    private String documentText;
    private int imageId;


    public DocumentText(String documentText, int imageId) {
        this.documentText = documentText;
        this.imageId = imageId;
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
                Objects.equals(getDocumentText(), that.getDocumentText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDocumentText(), getImageId());
    }

    @Override
    public String toString() {
        return "DocumentText{" +
                "id=" + id +
                ", documentText='" + documentText + '\'' +
                ", imageId=" + imageId +
                '}';
    }
}
