package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.model.dto.document.PostalAddress;
import com.capitalone.creditocr.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.capitalone.creditocr.util.TimeUtils.date2instant;

@Repository
public class PgDocumentDao implements DocumentDao {

    private static final Logger logger = LoggerFactory.getLogger(PgDocumentDao.class);

    private final DataSource dataSource;

    private static final RowMapper<Document> DOCUMENT_ROW_MAPPER = ((resultSet, rNum) -> {
        var doc = Document.builder()
                .setAccountNumber(resultSet.getLong("account_number"))
                .setSsn(resultSet.getString("ssn"))
                .setLetterDate(date2instant(resultSet.getDate("letter_date")))
                .setPostmarkDate(date2instant(resultSet.getDate("postmark_date")))
                .setNumSimilarDocuments(resultSet.getInt("num_similar_documents"))
                .setAddressId(resultSet.getInt("address"))
                .setFingerprint(resultSet.getBytes("fingerprint"))
                .build();

        doc.setId(resultSet.getInt("id"));
        return doc;
    });

    @Autowired
    public PgDocumentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @SuppressWarnings("Duplicates")
    @Override
    public void createDocument(@NonNull Document document) {
        Objects.requireNonNull(document);

        //language=sql
        String sql = "INSERT INTO document (account_number, ssn, letter_date, postmark_date, date_of_birth)" +
                         "VALUES (:acctNo, :ssn, :ldate, :pdate, :dob);";

        // TODO: insert address if non-null

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("acctNo", document.getAccountNumber() <= 0 ? null : document.getAccountNumber())
                .addValue("ssn", document.getSsn())
                .addValue("ldate", instant2SqlDate(document.getLetterDate()))
                .addValue("pdate", instant2SqlDate(document.getPostmarkDate()))
                .addValue("dob", instant2SqlDate(document.getDateOfBirth()));

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder, new String[] {"id"});
        Map<String, Object> keyMap = holder.getKeys();
        Objects.requireNonNull(keyMap);

        document.setId((Integer) keyMap.get("id"));
    }

    @Override
    public int getDocumentIDbyJob(int id) {
        //language=sql
        String sql = "SELECT document_image.document_id FROM document_images JOIN jobs " +
                "ON jobs.document_image=document_images.id WHERE jobs.id=:id";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id",id);

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        RowMapper<Integer> rmap = (results, rowNumber) -> results.getInt("document_id");
        List<Integer> idList = template.query(sql, source, rmap);

        return idList.get(0);
    }

    @Override
    public Optional<Document> getDocumentById(int id) {
        //language=sql
        String sql = " SELECT * FROM document WHERE id = :id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", id);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        List<Document> docs = template.query(sql,source, DOCUMENT_ROW_MAPPER);

        if (docs.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(docs.get(0));
        }
    }

    @Override
    public List<Integer> getSimilarDocumentIds(byte[] fingerprint, float sensitivity) {
        //language=sql
        String sql = " select id" +
                     " from document " +
                     " where document.fingerprint is not null " +
                     " and 1-(bytea_bitsset(bytea_xor(:fingerprint, document.fingerprint))/128::float) > :sensitivity;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("fingerprint", fingerprint)
                .addValue("sensitivity", sensitivity);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        return template.query(sql, source, (resultSet, rowNum) -> resultSet.getInt("id"));
    }

    @Override
    public void updateDocument(Document document) {
        logger.info("updating document. " + document);

        //language=sql
        //noinspection SqlWithoutWhere
        String sql = "UPDATE document SET num_similar_documents = :similarDocs ";
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("similarDocs", document.getNumSimilarDocuments());

        if (document.getAccountNumber() != null) {
            sql += ", account_number = :acctNo ";
            source.addValue("acctNo", document.getAccountNumber());
        }

        if (document.getSsn() != null) {
            sql += ", ssn = :ssn ";
            source.addValue("ssn", document.getSsn());
        }

        if (document.getLetterDate() != null) {
            sql += ", letter_date = :letterDate ";
            source.addValue("letterDate", TimeUtils.instant2date(document.getLetterDate()));
        }

        if (document.getFingerprint() != null) {
            sql += ", fingerprint = :fingerprint ";
            source.addValue("fingerprint", document.getFingerprint());
        }

        sql += " WHERE id = :id;";
        source.addValue("id", document.getId());

        var template = new NamedParameterJdbcTemplate(dataSource);
        template.update(sql, source);
    }

    @SuppressWarnings("Duplicates")
    @Override
    @Transactional
    public void setAddress(int documentId, PostalAddress address) {
        //language=sql
        String sql = " insert into addresses (first_line, second_line, city, state, postal_code, country) " +
                     " values (:fline, :sline, :city, :state, :zip, :country) ";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("fline", address.getFirstLine())
                .addValue("sline", address.getSecondLine())
                .addValue("city", address.getCity())
                .addValue("state", address.getState())
                .addValue("zip", address.getPostalCode())
                .addValue("country", address.getCountry());

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(sql, source, keyHolder, new String[] {"id"});
        Map<String, Object> idMap = keyHolder.getKeys();
        Objects.requireNonNull(idMap);

        address.setId((Integer) idMap.get("id"));

        sql = "update document set address = :addrId where document.id = :docId;";
        source = new MapSqlParameterSource()
                .addValue("addrId", address.getId())
                .addValue("docId", documentId);

        template.update(sql, source);
    }


    private static Date instant2SqlDate(@Nullable Instant instant) {
        if (instant == null) {
        return null;
        }
        return new Date(instant.toEpochMilli());
        }
        }
