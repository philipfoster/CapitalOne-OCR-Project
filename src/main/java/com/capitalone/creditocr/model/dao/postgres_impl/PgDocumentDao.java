package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.util.Simhash;
import com.capitalone.creditocr.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.capitalone.creditocr.util.TimeUtils.date2instant;

@Repository
public class PgDocumentDao implements DocumentDao {

    private static final Logger logger = LoggerFactory.getLogger( PgDocumentDao.class );

    private final DataSource dataSource;

    private static final RowMapper<Document> DOCUMENT_ROW_MAPPER = ((resultSet, rNum) -> mapRow( resultSet ));

    @Autowired
    public PgDocumentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public void createDocument(Document document) {
        Objects.requireNonNull( document );
        //language=sql
        String sql = "INSERT INTO document (account_number, ssn, letter_date, postmark_date, date_of_birth, queue)" +
                     "VALUES (:acctNo, :ssn, :ldate, :pdate, :dob, :queue);";

        // TODO: insert address if non-null

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue( "acctNo", (document.getAccountNumber() == null || document.getAccountNumber() <= 0) ? null : document.getAccountNumber() );
        source.addValue( "ssn", document.getSsn() );
        source.addValue( "ldate", instant2SqlDate( document.getLetterDate() ) );
        source.addValue( "pdate", instant2SqlDate( document.getPostmarkDate() ) );
        source.addValue( "dob", instant2SqlDate( document.getDateOfBirth() ) );
        source.addValue( "queue", (document.getQueue() == null) ? "general" : document.getQueue() );

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate( dataSource );
        KeyHolder holder = new GeneratedKeyHolder();

        template.update( sql, source, holder, new String[]{"id"} );
        Map<String, Object> keyMap = holder.getKeys();
        Objects.requireNonNull( keyMap );

        document.setId( (Integer) keyMap.get( "id" ) );
    }

    @Override
    public int getDocumentIDbyJob(int id) {
        //language=sql
        String sql = "SELECT document_images.document_id FROM document_images JOIN jobs " +
                     "ON jobs.document_image=document_images.id WHERE jobs.id=:id";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue( "id", id );

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate( dataSource );

        RowMapper<Integer> rmap = (results, rowNumber) -> results.getInt( "document_id" );
        List<Integer> idList = template.query( sql, source, rmap );

        return idList.get( 0 );
    }

    @Override
    public Optional<Document> getDocumentById(int id) {
        //language=sql
        String sql = " SELECT * FROM document WHERE id = :id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue( "id", id );
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate( dataSource );

        List<Document> docs = template.query( sql, source, DOCUMENT_ROW_MAPPER );

        if (docs.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of( docs.get( 0 ) );
        }
    }

    @Override
    public List<Integer> getSimilarDocumentIds(byte[] fingerprint, float sensitivity) {
        long[] split = Simhash.splitHash(fingerprint);

        //language=sql
        String sql = " select id" +
                     " from document " +
                     " where document.left_fingerprint != 0 " +
                     " and document.right_fingerprint !=  0 " +
                     " and 1-(length(regexp_replace((left_fingerprint # :leftHash)::bit(64)::text, '0', '', 'g')) + length(regexp_replace((right_fingerprint # :rightHash)::bit(64)::text, '0', '', 'g')))/128::float > :sensitivity;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue( "leftHash", split[0] )
                .addValue( "rightHash", split[1] )
//                .addValue( "fingerprint", fingerprint )
                .addValue( "sensitivity", sensitivity );
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate( dataSource );

        return template.query( sql, source, (resultSet, rowNum) -> resultSet.getInt( "id" ) );
    }

    @Override
    public void updateDocument(Document document) {
        logger.info( "updating document. " + document );

        //language=sql
        //noinspection SqlWithoutWhere -- Where clause is done later on.
        String sql = "UPDATE document SET num_similar_documents = :similarDocs ";
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue( "similarDocs", document.getNumSimilarDocuments() );

        if (document.getAccountNumber() != null) {
            sql += ", account_number = :acctNo ";
            source.addValue( "acctNo", document.getAccountNumber() );
        }

        if (document.getSsn() != null) {
            sql += ", ssn = :ssn ";
            source.addValue( "ssn", document.getSsn() );
        }

        if (document.getLetterDate() != null) {
            sql += ", letter_date = :letterDate ";
            source.addValue( "letterDate", TimeUtils.instant2date( document.getLetterDate() ) );
        }

        if (document.getDateOfBirth() != null) {
            sql += ", date_of_birth = :dob";
            source.addValue( "dob", TimeUtils.instant2date( document.getDateOfBirth() ) );
        }

        if (document.getFingerprint() != null) {
            long[] splitFprint = Simhash.splitHash( document.getFingerprint() );
            sql += ", left_fingerprint = :leftFprint, right_fingerprint = :rightFprint ";
            source.addValue( "leftFprint", splitFprint[0] );
            source.addValue( "rightFprint", splitFprint[1] );
        }

        if (document.getQueue() != null && !document.getQueue().isBlank()) {
            sql += ", queue = :queue ";
            source.addValue( "queue", document.getQueue() );
        }

        sql += " WHERE id = :id;";
        source.addValue( "id", document.getId() );

        var template = new NamedParameterJdbcTemplate( dataSource );
        template.update( sql, source );
    }

    @Override
    public int countDocumentPages(int documentId) {
        // language=sql
        String sql = " select count(id) as count " +
                     " from document_images " +
                     " where document_id = :id;";

        var source = new MapSqlParameterSource()
                .addValue( "id", documentId );
        var template = new NamedParameterJdbcTemplate( dataSource );

        return template.query( sql, source, (rs, __) -> rs.getInt( "count" ) ).get( 0 );
    }

    @Override
    public boolean hasEnvelope(int documentId) {
        //language=sql
        String sql = " select (count(id) > 0) as hasEnvelope " +
                     " from document_images " +
                     " where document_id = :id " +
                     " and is_envelope = true;";

        var source = new MapSqlParameterSource()
                .addValue( "id", documentId );
        var template = new NamedParameterJdbcTemplate( dataSource );

        return template.query( sql, source, ((rs, __) -> rs.getBoolean( "hasEnvelope" )) ).get( 0 );
    }


    @Override
    public List<Document> getDocumentsByKeyword(String keyword, int pageSize, int pageNum) {
        //language=sql
        String sql = " select distinct document.*, ts_rank_cd(vectorized_text || to_tsvector(lower(queue)), to_tsquery(:searchString)) as rank " +
                     " from document " +
                     " inner join document_images image on document.id = image.document_id " +
                     " inner join document_text text on image.id = text.image_id " +
                     " where (vectorized_text || to_tsvector(lower(queue))) @@ to_tsquery(:searchString) " +
                     " order by rank DESC " +
                     " limit :limit " +
                     " offset :limit * :pageNum";

        var source = new MapSqlParameterSource()
                .addValue( "searchString", keyword )
                .addValue( "limit", pageSize )
                .addValue( "pageNum", pageNum );

        var template = new NamedParameterJdbcTemplate( dataSource );
        return template.query( sql, source, DOCUMENT_ROW_MAPPER);
    }


    private static Document mapRow(ResultSet resultSet) throws SQLException {
        var doc = Document.builder()
                .setAccountNumber( resultSet.getLong( "account_number" ) )
                .setSsn( resultSet.getString( "ssn" ) )
                .setLetterDate( date2instant( resultSet.getDate( "letter_date" ) ) )
                .setPostmarkDate( date2instant( resultSet.getDate( "postmark_date" ) ) )
                .setNumSimilarDocuments( resultSet.getInt( "num_similar_documents" ) )
                .setDateOfBirth( date2instant( resultSet.getDate( "date_of_birth" ) ) )
                .setAddressId( resultSet.getInt( "address" ) )
//                .setFingerprint( resultSet.getBytes( "fingerprint" ) )
                .setQueue( resultSet.getString( "queue" ) )
                .build();

        long[] fprint = new long[2];
        fprint[0] = resultSet.getLong( "left_fingerprint" );
        fprint[1] = resultSet.getLong( "right_fingerprint" );
        doc.setFingerprint( Simhash.unsplitHash( fprint ) );

        doc.setId( resultSet.getInt( "id" ) );
        return doc;
    }

    private static Date instant2SqlDate(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        }
        return new Date( instant.toEpochMilli() );
    }
}
