package com.capitalone.creditocr.model.dto.document_image;

import com.capitalone.creditocr.model.dto.ImageType;

public class DocumentImageDtoBuilder {
    private byte[] fileData;
    private int pageNumber = -1;
    private boolean isEnvelope = false;
    private ImageType imageType;

    public DocumentImageDtoBuilder setFileData(byte[] fileData) {
        this.fileData = fileData;
        return this;
    }


    public DocumentImageDtoBuilder setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public DocumentImageDtoBuilder setIsEnvelope(boolean isEnvelope) {
        this.isEnvelope = isEnvelope;
        return this;
    }

    public DocumentImageDtoBuilder setImageType(ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    public DocumentImageDto build() {
        return new DocumentImageDto(fileData, pageNumber, isEnvelope, imageType);
    }
}