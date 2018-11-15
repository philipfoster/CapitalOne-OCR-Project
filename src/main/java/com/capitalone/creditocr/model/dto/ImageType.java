package com.capitalone.creditocr.model.dto;

import org.springframework.lang.Nullable;

import java.nio.file.Path;

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
    public static ImageType fromContentType(@Nullable String contentType) {
        if (contentType == null) {
            return null;
        }

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

    /**
     * Convert a file {@link Path} to an image type.
     * @param path The path to the file
     * @return The image type, or {@code null} if there is no relevant image type
     */
    @Nullable
    public static ImageType fromPath(Path path) {
        String[] filename = path.toFile().getName().toLowerCase().split("\\.");
        String extension = filename[filename.length-1];

        switch (extension) {
            case "jpg":
            case "jpeg":
                return JPEG;
            case "png":
                return PNG;
            case "pdf":
                return PDF;
            case "tif":
            case "tiff":
                return TIFF;
            default:
                return null;

        }
    }
}
