package com.capitalone.creditocr.model.dto.document_image;


import com.capitalone.creditocr.model.dto.ImageType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents a row in the document_images table.
 */
@SuppressWarnings("WeakerAccess")
public final class DocumentImage {

    public static final int PAGE_NUM_ENVELOPE = -1;


    private int id;
    private byte[] fileData;
    private int pageNumber;
    private boolean isEnvelope;
    private ImageType imageType;
    private int documentId;

    DocumentImage(byte[] fileData, int pageNumber, boolean isEnvelope, ImageType imageType, int documentId) {
        this.fileData = fileData;
        this.pageNumber = pageNumber;
        this.isEnvelope = isEnvelope;
        this.imageType = imageType;
        this.documentId = documentId;
    }

    public static DocumentImageBuilder builder() {
        return new DocumentImageBuilder();
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

    public BufferedImage toBufferedImage() {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(fileData)) {
            return ImageIO.read(bytes);
        } catch (IOException e) {
//            logger.error("Could not read image", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentImage that = (DocumentImage) o;
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
        return "DocumentImage{" +
                "id=" + id +
                ", fileData=" + fileData.length + " bytes "+
                ", pageNumber=" + pageNumber +
                ", isEnvelope=" + isEnvelope +
                ", imageType=" + imageType +
                ", documentId=" + documentId +
                '}';
    }
}
