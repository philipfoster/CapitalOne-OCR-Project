package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentTextDao;
import com.capitalone.creditocr.model.dto.Join2;
import com.capitalone.creditocr.model.dto.document.DocumentText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PgDocumentTextDao implements DocumentTextDao {

    private final DataSource dataSource;

    private static final RowMapper<Join2<Integer, DocumentText>> DOCUMENT_PAGE_MAPPER = ((resultSet, rowNum) -> {
        var text = new DocumentText(
                resultSet.getString("original_text"),
        resultSet.getInt("image_id")
        );

        text.setId(resultSet.getInt("id"));
        //noinspection unchecked
        return Join2.<Integer, DocumentText>builder()
                .left(resultSet.getInt("page_number"))
                .right(text)
                .build();
    });

    @Autowired
    public PgDocumentTextDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Optional<String> getDocumentTextById(int id) {
        //language=sql
        String sql = " select original_text as text " +
                     " from document_images image " +
                     "        inner join document_text dt on image.id = dt.image_id " +
                     " where document_id = :docId " +
                     " order by image.page_number asc; ";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("docId",  id);

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        List<String> result = template.query(sql, source, (rSet, rNum) -> rSet.getString("text"));


        if (result.isEmpty()) {
            return Optional.empty();
        } else {
            StringBuilder ret = new StringBuilder();
            for (String s : result) {
                ret.append( s );
            }
            return Optional.of( ret.toString() );
        }
    }

    @Override
    public void addDocumentText(DocumentText documentText) {
        //language=sql
        String sql = "INSERT INTO document_text (original_text, image_id) " +
                     "     VALUES (:docText, :imageId);";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("docText", documentText.getDocumentText())
                .addValue("imageId", documentText.getImageId());

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder);
        Map<String, Object> keys = holder.getKeys();
        Objects.requireNonNull(keys);

        documentText.setId((Integer) keys.get("id"));
    }

    @Override
    public String getFullDocumentText(int documentId) {
        //language=sql
        String sql = " SELECT document_text.id, original_text, image_id, images.page_number " +
                     " FROM document_text " +
                     " JOIN document_images images on document_text.image_id = images.id" +
                     " WHERE images.document_id = :docId " +
                     "  AND NOT images.is_envelope";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("docId", documentId);

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        // Copy list of entries into a map
        var entries = template.query(sql, source, DOCUMENT_PAGE_MAPPER);

        return entries.stream()
                .sorted(Comparator.comparingInt(Join2::getLeft))
                .map(it -> it.getRight().getDocumentText())
                .collect(Collectors.joining());
    }
}
