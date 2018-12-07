package com.capitalone.creditocr.controller.search_document;


import com.capitalone.creditocr.controller.exception.FileNotFoundException;
import com.capitalone.creditocr.view.DocumentResponse;
import com.capitalone.creditocr.view.factory.DocumentResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class SearchByIdController {

    private final DocumentResponseFactory responseFactory;


    @Autowired
    public SearchByIdController(DocumentResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    /**
     * Get a document by ID
     * @param id the document ID
     * @return the document
     */
    @GetMapping("documents/{id}")
    public DocumentResponse processRequest(@PathVariable("id") int id) {

        Optional<DocumentResponse> responseOptional = responseFactory.getResponse( id );
        if (responseOptional.isEmpty()) {
            throw new FileNotFoundException( String.format( "Document with id %d does not exit", id ) );
        }

        return responseOptional.get();
    }

}
