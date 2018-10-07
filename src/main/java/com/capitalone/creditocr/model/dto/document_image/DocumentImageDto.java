package com.capitalone.creditocr.model.dto.document_image;


import com.capitalone.creditocr.model.dto.ImageType;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents a row in the document_images table.
 */
public final class DocumentImageDto {

    private int id;
    private byte[] fileData;
    private String fileName;
    private int pageNumber;
    private boolean isEnvelope;
    private ImageType imageType;

    DocumentImageDto(byte[] fileData, String fileName, int pageNumber,
                     boolean isEnvelope, ImageType imageType) {
        this.fileData = fileData;
        this.fileName = fileName;
        this.pageNumber = pageNumber;
        this.isEnvelope = isEnvelope;
        this.imageType = imageType;
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

    public String getFileName() {
        return fileName;
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

    @Override
    public String toString() {
        return "DocumentImageDto{" +
                "id=" + id +
                ", fileData=" + Arrays.toString(fileData) +
                ", fileName='" + fileName + '\'' +
                ", pageNumber=" + pageNumber +
                ", isEnvelope=" + isEnvelope +
                ", imageType=" + imageType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentImageDto that = (DocumentImageDto) o;
        return getId() == that.getId() &&
                getPageNumber() == that.getPageNumber() &&
                isEnvelope() == that.isEnvelope() &&
                Arrays.equals(getFileData(), that.getFileData()) &&
                Objects.equals(getFileName(), that.getFileName()) &&
                getImageType() == that.getImageType();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getFileName(), getPageNumber(), isEnvelope(), getImageType());
        result = 31 * result + Arrays.hashCode(getFileData());
        return result;
    }
}
