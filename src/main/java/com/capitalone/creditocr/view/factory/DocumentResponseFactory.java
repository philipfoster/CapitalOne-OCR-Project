package com.capitalone.creditocr.view.factory;

import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.view.DocumentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This factory will build a {@link DocumentResponse}
 */
@Service
public class DocumentResponseFactory {

    private final DocumentDao documentDao;

    @Autowired
    public DocumentResponseFactory(DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    /**
     * Get a document response by re-using an existing {@code Document}
     */
    public DocumentResponse getResponse(Document document) {
        DocumentResponse response = new DocumentResponse( document );

        int numPages = documentDao.countDocumentPages( document.getId() );
        response.setNumPages( numPages );

        boolean hasEnvelope = documentDao.hasEnvelope( document.getId() );
        response.setHasEnvelope( hasEnvelope );

        // TODO: get address

        return response;
    }

    /**
     * Get a document response by the document ID, if the requested document exists
     *
     * @param docId the document Id
     */
    public Optional<DocumentResponse> getResponse(int docId) {

        Optional<Document> documentOptional = documentDao.getDocumentById( docId );

        if (documentOptional.isEmpty()) {
            return Optional.empty();
        } else {
            var document = documentOptional.get();
            return Optional.of( getResponse( document ) );
        }

    }

}
