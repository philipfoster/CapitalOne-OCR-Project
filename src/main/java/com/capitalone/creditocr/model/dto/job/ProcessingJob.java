package com.capitalone.creditocr.model.dto.job;


import java.time.Instant;
import java.util.Objects;

/**
 * DTO for jobs table
 */
public class ProcessingJob {

    private int id;
    private Instant creationTime;
    private int imageFk;

    public ProcessingJob(int id, Instant creationTime, int imageFk) {
        this.id = id;
        this.creationTime = creationTime;
        this.imageFk = imageFk;
    }

    public ProcessingJob(Instant creationTime, int imageFk) {
        this.creationTime = creationTime;
        this.imageFk = imageFk;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public int getImageFk() {
        return imageFk;
    }

    @Override
    public String toString() {
        return "ProcessingJob{" +
                "id=" + id +
                ", creationTime=" + creationTime +
                ", imageFk=" + imageFk +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingJob that = (ProcessingJob) o;
        return getId() == that.getId() &&
                getImageFk() == that.getImageFk() &&
                Objects.equals(getCreationTime(), that.getCreationTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreationTime(), getImageFk());
    }
}
