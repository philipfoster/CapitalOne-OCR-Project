package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dto.ImageType;
import com.capitalone.creditocr.model.dto.document_image.DocumentImageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

import static com.capitalone.creditocr.model.dto.document_image.DocumentImageDto.PAGE_NUM_ENVELOPE;

// Note to team:
// One of the core pieces of Spring is it's dependency injection (DI; https://en.wikipedia.org/wiki/Dependency_injection)
// framework, which makes other classes that this one uses (dependencies) easily swappable with minimal changes. This is
// especially helpful when future changes need to be made, or for testing, where dummy/mock objects can be replaced with the
// real ones. Internally, spring looks at all classes which are registered with it for DI, and creates a "dependency graph",
// where it will create all the required dependencies to create this object when it is requested.
//
// There are several annotations that tell spring that a class should participate in the dependency graph, each with a few
// very minor differences. The best summary I've found of the differences are outlined in this answer on StackOverflow:
// https://stackoverflow.com/a/6897038/1021064
//
// The Autowired annotation tells spring where to look when it needs to check which dependencies this object needs before its
// constructor is called. You _can_ put this on the field, however best-practice dictates that you should use constructor injection
// instead of field-based injection, for when you need to construct an instance of this object outside of spring (i.e. in test cases)
@Repository
public class PgDocumentImageDao implements DocumentImageDao {

    private static final Logger logger = LoggerFactory.getLogger(PgDocumentImageDao.class);

    private final DataSource dataSource;


    private static final RowMapper<DocumentImageDto> ROW_MAPPER = (rs, rowNum) -> DocumentImageDto.builder()
            .setFileData(rs.getBytes("file_data"))
            .setImageType(ImageType.valueOf(rs.getString("image_format")))
            .setPageNumber(rs.getInt("page_number"))
            .setIsEnvelope(rs.getBoolean("is_envelope"))
            .build();

    @Autowired
    public PgDocumentImageDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


//    @Override
//    public void addNewImage(byte[] data, ImageType imageType, int pageNum) {
//        //language=sql
//        String sql = "INSERT INTO document_images (file_data, page_number, image_format, is_envelope) " +
//                     "      VALUES (:fileData, :pageNum, :format::image_format, :isEnvelope);";
//
//        MapSqlParameterSource source = new MapSqlParameterSource()
//                .addValue("fileData", data)
//                .addValue("pageNum", pageNum == PAGE_NUM_ENVELOPE ? null : pageNum)
//                .addValue("format", imageType.toString())
//                .addValue("isEnvelope", pageNum == PAGE_NUM_ENVELOPE);
//
//        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
//        template.update(sql, source);
//    }

    @Override
    public void addNewImage(DocumentImageDto image) {

        String sql = "INSERT INTO document_images (file_data, page_number, image_format, is_envelope) " +
                "      VALUES (:fileData, :pageNum, :format::image_format, :isEnvelope);";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("fileData", image.getFileData())
                .addValue("pageNum", image.getPageNumber() == PAGE_NUM_ENVELOPE ? null : image.getPageNumber())
                .addValue("format", image.getImageType().toString())
                .addValue("isEnvelope", image.getPageNumber() == PAGE_NUM_ENVELOPE);

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder, new String[] {"id"});
        Map<String, Object> keyMap = holder.getKeys();
        Objects.requireNonNull(keyMap); // Throw a NPE if the value is null, but make the warning go away.

        image.setId((Integer) keyMap.get("id"));

    }
}
