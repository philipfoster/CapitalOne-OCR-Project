package com.capitalone.creditocr.model.dao.postgres_impl;

import com.capitalone.creditocr.controller.job_status.JobStatusEnum;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.job.JobType;
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
import java.sql.Date;
import java.time.Instant;
import java.util.*;



@Repository
public class PgJobDao implements JobDao {

    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(PgJobDao.class);

    private static final RowMapper<ProcessingJob> ROW_MAPPER = (rs, rowNum) -> {
        int id = rs.getInt("id");
        Instant created = TimeUtils.date2instant(rs.getDate("created_at"));
        JobType jobType = JobType.valueOf(rs.getString("type").toUpperCase());

        switch (jobType) {
            case IMAGE:
                var job = ProcessingJob.imageJob(created, rs.getInt("document_image"));
                job.setId(id);
                return job;
            case FINGERPRINT:
                job = ProcessingJob.documentJob(created, rs.getInt("document_id"));
                job.setId(id);
                return job;
            default:
                // Should never happen, but it makes the compiler happy...
                return null;
        }
    };

    private static final RowMapper<Map<String, Date>> DATE_ROW_MAPPER = (rs, rowNum) -> {
        Date accepted = rs.getDate("accepted_at");
        Date completed = rs.getDate("completed_at");

        Map<String, Date> assignmentMap = new HashMap<>();
        assignmentMap.put("accepted_at", accepted);
        assignmentMap.put("completed_at", completed);

        return assignmentMap;
    };

    @Autowired
    public PgJobDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }



    @Override
    public void createImageProcessingJob(ProcessingJob job) {
        // language=sql
        String sql = "INSERT INTO jobs (created_at, document_image, type, document_id) " +
                     "     VALUES (:ctime, :image, 'image'::job_type, null);";

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
    @Transactional
    public void createDocumentProcessingJob(ProcessingJob job, List<Integer> dependencies) {
        // Inset job itself
        //language=sql
        String sql = "INSERT INTO jobs (created_at, type, document_id, document_image) " +
                     "     VALUES (:ctime, 'fingerprint'::job_type, :docId, null);";

        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("ctime", TimeUtils.instant2date(job.getCreationTime()))
                .addValue("docId", job.getDocumentFk());
        KeyHolder holder = new GeneratedKeyHolder();

        template.update(sql, source, holder, new String[] {"id"});
        Map<String, Object> keyMap = holder.getKeys();
        Objects.requireNonNull(keyMap);

        int id = (int) keyMap.get("id");
        job.setId(id);

        // Add dependencies
        sql = "INSERT INTO job_dependency (job, dependency) " +
                     "     VALUES (:job, :dep);";

        for (Integer dependency : dependencies) {
            source = new MapSqlParameterSource()
                    .addValue("job", id)
                    .addValue("dep", dependency);

            template.update(sql, source);
        }
    }

    @Override
    public List<ProcessingJob> getAvailableJobs(int pageSize, int pageNum) {
        //language=sql
        String sql = " WITH dep_completion_status AS (" +
                     "   select dep.job job, every(completed_at IS NOT null) isReady" +  // The set of job IDs with at least 1 dependency, matched with its dependency completion status
                     "   from jobs" +
                     "   inner join job_dependency dep on jobs.id = dep.dependency" +
                     "   left join job_assignments assign on jobs.id = assign.job_id" +
                     "   group by dep.job" +
                     "   union" +  // Hack to include jobs with no dependencies, which don't appear in the first part
                     "   select jobs.id, true isReady" +
                     "   from jobs" +
                     "   where jobs.type = 'image'::job_type" +
                     " ) " +
                     " SELECT * FROM jobs WHERE jobs.id IN (" +
                     "   SELECT job FROM dep_completion_status status WHERE status.isReady" +
                     "   EXCEPT" + // remove jobs which are have already been or are being processed
                     "   SELECT job_id from job_assignments" +
                     " ) " +
                     " order by jobs.created_at " +
                     " limit  :page_size OFFSET :page_size * :page_num;";

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

    @Override
    public Optional<ProcessingJob> getJobByID(int id) {
        //language=sql
        String sql = "SELECT * FROM jobs WHERE id = :id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", id);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        List<ProcessingJob> jobList = template.query(sql, source, ROW_MAPPER);

        // If no job is found, the list is considered empty.
        if (jobList.isEmpty()) {
            return Optional.empty();
        }
        // Otherwise, the list should only have value, so return the first value.
        else {
            return Optional.of(jobList.get(0));
        }

    }

    @Override
    public JobStatusEnum getJobStatus(int id) {
        //language=sql
        String sql = "SELECT * FROM job_assignments WHERE job_id = :id;";

        MapSqlParameterSource source = new MapSqlParameterSource()
                .addValue("id", id);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        List<Map<String,Date>> jobList = template.query(sql, source, DATE_ROW_MAPPER);

        if (jobList.isEmpty()) {
            return JobStatusEnum.PENDING;
        }
        else {
            if (jobList.get(0).get("completed_at") == null) {
                return JobStatusEnum.PROCESSING;
            }
            else {
                return JobStatusEnum.COMPLETED;
            }
        }
    }
}
