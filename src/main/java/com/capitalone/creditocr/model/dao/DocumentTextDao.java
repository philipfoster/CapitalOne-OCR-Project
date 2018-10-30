package com.capitalone.creditocr.model.dao;

import com.capitalone.creditocr.model.dto.document.DocumentText;

import java.util.Optional;

public interface DocumentTextDao {


    /**
     * Load the text for a specific doucment.
     * @param id the ID of the document to load
     * @return the document text
     */
    Optional<String> getDocumentTextById(int id);

    /**
     * Add text to a document
     * @param documentText The data to insert
     */
    void addDocumentText(DocumentText documentText);

}
