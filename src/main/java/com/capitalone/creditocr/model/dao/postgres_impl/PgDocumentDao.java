package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dto.document.DocumentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Repository
public class PgDocumentDao implements DocumentDao {


    private final DataSource dataSource;

    @Autowired
    public PgDocumentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public void createDocument(DocumentDto document) {
        //language=sql
        String sql = "INSERT INTO document (account_number, ssn, letter_date, postmark_date, date_of_birth)" +
                         "VALUES (:acctNo, :ssn, :ldate, :pdate, :dob);";

        // TODO: insert address if non-null

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("acctNo", document.getAccountNumber() > 0 ? document.getAccountNumber() : null)
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


    private static Date instant2SqlDate(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        }
        return new Date(instant.toEpochMilli());
    }
}
