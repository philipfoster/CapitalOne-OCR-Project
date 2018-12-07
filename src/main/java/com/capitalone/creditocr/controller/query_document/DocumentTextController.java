package com.capitalone.creditocr.controller.query_document;

import com.capitalone.creditocr.controller.exception.FileNotFoundException;
import com.capitalone.creditocr.model.dao.DocumentTextDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class DocumentTextController {

    private final DocumentTextDao documentTextDao;

    @Autowired
    public DocumentTextController(DocumentTextDao documentTextDao) {
        this.documentTextDao = documentTextDao;
    }

    @GetMapping("/documents/{id}/text")
    public String processRequest(@PathVariable("id") int id) {

        Optional<String> textOptional = documentTextDao.getDocumentTextById( id );

        if (textOptional.isEmpty()) {
            throw new FileNotFoundException( String.format( "Document %d does not exist", id) );
        }

        return textOptional.get();

    }


}
