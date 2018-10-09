package com.capitalone.creditocr.model.dao;

import com.capitalone.creditocr.model.dto.document.Document;


public interface DocumentDao {

    /**
     * This method will insert a document into the database. The {@link Document#id} field will also be
     * set with the unique ID that was automatically generated by the database.
     * @param document The document to insert.
     */
    void createDocument(Document document);

}
