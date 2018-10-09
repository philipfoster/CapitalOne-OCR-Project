package com.capitalone.creditocr.model.dto.document_image;


import com.capitalone.creditocr.model.dto.ImageType;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents a row in the document_images table.
 */
@SuppressWarnings("WeakerAccess")
public final class DocumentImageDto {

    public static final int PAGE_NUM_ENVELOPE = -1;


    private int id;
    private byte[] fileData;
    private int pageNumber;
    private boolean isEnvelope;
    private ImageType imageType;
    private int documentId;

    DocumentImageDto(byte[] fileData, int pageNumber, boolean isEnvelope, ImageType imageType, int documentId) {
        this.fileData = fileData;
        this.pageNumber = pageNumber;
        this.isEnvelope = isEnvelope;
        this.imageType = imageType;
        this.documentId = documentId;
    }

    public static DocumentImageDtoBuilder builder() {
        return new DocumentImageDtoBuilder();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public boolean isEnvelope() {
        return isEnvelope;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public int getDocumentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentImageDto that = (DocumentImageDto) o;
        return getId() == that.getId() &&
                getPageNumber() == that.getPageNumber() &&
                isEnvelope() == that.isEnvelope() &&
                documentId == that.documentId &&
                Arrays.equals(getFileData(), that.getFileData()) &&
                getImageType() == that.getImageType();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getPageNumber(), isEnvelope(), getImageType(), getDocumentId());
        result = 31 * result + Arrays.hashCode(getFileData());
        return result;
    }

    @Override
    public String toString() {
        return "DocumentImageDto{" +
                "id=" + id +
                ", fileData=" + Arrays.toString(fileData) +
                ", pageNumber=" + pageNumber +
                ", isEnvelope=" + isEnvelope +
                ", imageType=" + imageType +
                ", documentId=" + documentId +
                '}';
    }
}
