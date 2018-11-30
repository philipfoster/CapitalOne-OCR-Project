package com.capitalone.creditocr.controller.update_document;

import com.capitalone.creditocr.controller.exception.BadRequestException;
import com.capitalone.creditocr.controller.exception.FileNotFoundException;
import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.view.DocumentResponse;
import com.capitalone.creditocr.view.factory.DocumentResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;

@RestController
public class UpdateDocumentController {

    private final DocumentDao documentDao;
    private final DocumentResponseFactory responseFactory;

    private static final Set<String> QUEUES = Set.of( "fraud", "validation", "general", "inquiries" );
    private static final String SSN_REGEX = "[0-9]{3}-?[0-9]{2}-?[0-9]{4}";

    @Autowired
    public UpdateDocumentController(DocumentDao documentDao, DocumentResponseFactory responseFactory) {
        this.documentDao = documentDao;
        this.responseFactory = responseFactory;
    }

    @PatchMapping("/documents/{id}")
    @Transactional
    public DocumentResponse processRequest(
            @PathVariable("id") int id,
            @Nullable @RequestParam(value = "accountNumber", required = false) Long acctNo,
            @Nullable @RequestParam(value = "ssn", required = false) String ssn,
            @Nullable @RequestParam(value = "dateOfBirth", required = false) Long dob,
            @Nullable @RequestParam(value = "postmarkDate", required = false) Long postmarkDate,
            @Nullable @RequestParam(value = "letterDate", required = false) Long letterDate,
            @Nullable @RequestParam(value = "disputeQueue", required = false) String queue) {

        // TODO: Investigate if it is desirable to keep an audit log of manual updates.

        validateSsn( ssn );
        validateQueue( queue );

        var documentOptional = documentDao.getDocumentById( id );
        if (documentOptional.isEmpty()) {
            throw new FileNotFoundException( String.format( "Job with id %d does not exist", id ) );
        }

        Document document = documentOptional.get();
        if (acctNo != null) {
            document.setAccountNumber( acctNo );
        }

        if (ssn != null) {
            document.setSsn( ssn );
        }

        if (dob != null) {
            document.setDateOfBirth( Instant.ofEpochSecond( dob ) );
        }

        if (postmarkDate != null) {
            document.setPostmarkDate( Instant.ofEpochSecond( postmarkDate ) );
        }

        if (letterDate != null) {
            document.setLetterDate( Instant.ofEpochSecond( letterDate ) );
        }

        if (queue != null) {
            document.setQueue( queue.toUpperCase() );
        }

        documentDao.updateDocument( document );

        return responseFactory.getResponse( document );
    }

    /**
     * Throws an IllegalArgumentException if the input string is not a valid queue. If the parameter is null, the method
     * will not throw
     */
    private void validateQueue(@Nullable String queue) {
        if (queue == null) {
            return;
        }

        if (!QUEUES.contains( queue.toLowerCase() )) {
            throw new BadRequestException( String.format( "Queue string [%s] is not a valid queue", queue ) );
        }
    }

    /**
     * Throws an IllegalArgumentException if the input string does not match the regex. If the parameter is null,
     * the method will not throw.
     */
    private void validateSsn(@Nullable String ssn) {
        if (ssn == null) {
            return;
        }

        if (!ssn.matches( SSN_REGEX )) {
            throw new BadRequestException( String.format( "SSN string [%s] does not match a valid SSN format", ssn ) );
        }
    }


}
