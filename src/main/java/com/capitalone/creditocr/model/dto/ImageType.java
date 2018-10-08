package com.capitalone.creditocr.model.dto;

import org.springframework.lang.Nullable;

/**
 * This class mirrors the image_type custom enum type created in the database schema.
 */
public enum ImageType {
    JPEG,
    PDF,
    PNG,
    TIFF;

    ImageType() {
    }

    /**
     * Convert a HTTP Content-Type header to an ImageType.
     * @param contentType the content type to convert
     * @return The ImageType, or {@code null} if there is no matching enum constant.
     */
    @Nullable
    public static ImageType fromContentType(String contentType) {
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return JPEG;
            case "image/png":
                return PNG;
            case "image/tiff":
                return TIFF;
            case "application/pdf":
                return PDF;
            default:
                return null;
        }
    }
}
