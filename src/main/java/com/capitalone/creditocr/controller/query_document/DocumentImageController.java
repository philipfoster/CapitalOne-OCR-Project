package com.capitalone.creditocr.controller.query_document;

import com.capitalone.creditocr.controller.exception.FileNotFoundException;
import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dto.document_image.DocumentImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class DocumentImageController {

//    private static final Logger logger = LoggerFactory.getLogger( DocumentImageController.class );
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private final DocumentImageDao imageDao;

    @Autowired
    public DocumentImageController(DocumentImageDao imageDao) {
        this.imageDao = imageDao;
    }


    @RequestMapping(value = "/documents/{id}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> processRequest(@PathVariable("id") int id,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "envelope", required = false) String envelope) {

        Optional<DocumentImage> imageOptional;

        if (envelope == null) {
            imageOptional = imageDao.getImageForDocument( id, page );
        } else {
            imageOptional = imageDao.getEnvelopeForDocument( id );
        }

        if (imageOptional.isEmpty()) {
            throw new FileNotFoundException( "That image does not exist" );
        }

        var image = imageOptional.get();

        HttpHeaders headers = new HttpHeaders();
        headers.add( HEADER_CONTENT_TYPE, image.getImageType().toContentType() );

        return new ResponseEntity<>( image.getFileData(), headers, HttpStatus.OK );
    }


}
