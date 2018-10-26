package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import com.capitalone.creditocr.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.*;

@Repository
public class PgJobDao implements JobDao {

    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(PgJobDao.class);

    private static final RowMapper<ProcessingJob> ROW_MAPPER = (rs, rowNum) -> {
        int id = rs.getInt("id");
        Instant created = TimeUtils.date2instant(rs.getDate("created_at"));
        int imgId = rs.getInt("document_image");

        return new ProcessingJob(id, created, imgId);
    };

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

    @Override
    public List<ProcessingJob> getAvailableJobs(int pageSize, int pageNum) {
        //language=sql
        String sql = "SELECT * FROM jobs WHERE id IN (" +
                        "SELECT id FROM jobs " +
                        "EXCEPT " +
                        "SELECT job_id FROM job_assignments" +
                     ") ORDER BY created_at" +
                     "  LIMIT :page_size OFFSET :page_size * :page_num;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("page_size", pageSize)
                .addValue("page_num", pageNum);

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        return template.query(sql, source, ROW_MAPPER);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Optional<ProcessingJob> acceptNextJob(UUID serverId) {
        List<ProcessingJob> jobs = getAvailableJobs(1 ,0);

        if (jobs.isEmpty()) {
            return Optional.empty();
        }

        ProcessingJob job = jobs.get(0);

        //language=sql
        String sql = "INSERT INTO job_assignments (server_id, accepted_at, job_id) VALUES " +
                     "(:server_id, now(), :job_id)";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("server_id", serverId)
                .addValue("job_id", job.getId());
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        template.update(sql, source);

        return Optional.of(job);
    }

    @Override
    public void completeJob(ProcessingJob job) {
        //language=sql
        String sql = "UPDATE job_assignments set completed_at = now() " +
                     " WHERE job_id = :id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", job.getId());
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        template.update(sql, source);

    }
}
