package com.capitalone.creditocr.model.dto.document_image;

import com.capitalone.creditocr.model.dto.ImageType;

public class DocumentImageBuilder {
    private byte[] fileData;
    private int pageNumber = -1;
    private boolean isEnvelope = false;
    private ImageType imageType;
    private int documentId;

    public DocumentImageBuilder setFileData(byte[] fileData) {
        this.fileData = fileData;
        return this;
    }


    public DocumentImageBuilder setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public DocumentImageBuilder setIsEnvelope(boolean isEnvelope) {
        this.isEnvelope = isEnvelope;
        return this;
    }

    public DocumentImageBuilder setImageType(ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    public DocumentImageBuilder setDocumentId(int documentId) {
        this.documentId = documentId;
        return this;
    }

    public DocumentImage build() {
        return new DocumentImage(fileData, pageNumber, isEnvelope, imageType, documentId);
    }
}