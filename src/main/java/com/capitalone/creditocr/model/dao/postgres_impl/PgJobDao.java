package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import com.capitalone.creditocr.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

@Repository
public class PgJobDao implements JobDao {

    private final DataSource dataSource;

    @Autowired
    public PgJobDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createJob(ProcessingJob job) {
        // language=sql
        String sql = "INSERT INTO jobs (created_at, document_image) " +
                     "     VALUES (:ctime, :image);";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("ctime", TimeUtils.instant2date(job.getCreationTime()))
                .addValue("image", job.getImageFk());

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder, new String[] {"id"});
        Map<String, Object> keyMap = holder.getKeys();
        Objects.requireNonNull(keyMap);

        job.setId((Integer) keyMap.get("id"));
    }
}
