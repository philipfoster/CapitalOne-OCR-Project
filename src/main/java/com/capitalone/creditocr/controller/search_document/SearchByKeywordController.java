package com.capitalone.creditocr.controller.search_document;

import com.capitalone.creditocr.model.dao.postgres_impl.PgDocumentDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.view.DocumentResponse;
import com.capitalone.creditocr.view.factory.DocumentResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchByKeywordController {

    private static final Logger logger = LoggerFactory.getLogger( SearchByKeywordController.class );

    private final PgDocumentDao documentDao;
    private final DocumentResponseFactory responseFactory;

    @Autowired
    public SearchByKeywordController(PgDocumentDao documentDao, DocumentResponseFactory responseFactory) {
        this.documentDao = documentDao;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/documents/search")
    public List<DocumentResponse> processRequest(
            @RequestParam("query") String query,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {

        StringBuilder builder = new StringBuilder(  );
        String[] split = query.split( " " );

        // Filter out cases where multiple spaces were typed between words
        split = Arrays.stream( split )
                .filter( x -> !x.isBlank() )
                .toArray( String[]::new );

        // Postgres wants words to be separated by an operator, so split them with an OR
        for (int i = 0; i < split.length; i++) {
            String word = split[i];
            builder.append( word );

            if (i < split.length-1) {
                builder.append( " | " );
            }
        }

        logger.info( String.format( "query = %s", builder.toString() ) );

        List<Document> searchResults = documentDao.getDocumentsByKeyword( builder.toString(), pageSize, page );

        // Map document list into DocumentResponse list
        return searchResults.stream()
                .map( responseFactory::getResponse )
                .collect( Collectors.toList() );

    }

}
