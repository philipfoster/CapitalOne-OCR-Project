package com.capitalone.creditocr.model.dao;

import com.capitalone.creditocr.model.dto.ImageType;
import com.capitalone.creditocr.model.dto.document_image.DocumentImageDto;

/**
 * DAO interface for {@link DocumentImageDto}
 */
public interface DocumentImageDao {

    int PAGE_NUM_ENVELOPE = -1;

    /**
     * This method adds a new page to the document_images table. Additionally, a job will be created for the
     * image to be processed by an image processing worker.
     * @param data the raw image data
     * @param imageType the {@code data} file type
     * @param pageNum The page number, or {@link #PAGE_NUM_ENVELOPE} if this is an envelope.
     */
    void addNewImage(byte[] data, ImageType imageType, int pageNum);

}
