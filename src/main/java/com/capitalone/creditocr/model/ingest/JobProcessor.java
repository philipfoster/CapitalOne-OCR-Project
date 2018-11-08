package com.capitalone.creditocr.model.ingest;

import com.capitalone.creditocr.conf.InstanceConfig;
import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dao.DocumentTextDao;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.document.DocumentText;
import com.capitalone.creditocr.model.dto.document_image.DocumentImage;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class JobProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobProcessor.class);

    // TODO: Make this configurable.
    private static final int JOB_QUEUE_SIZE = 4;
    private static final long MS_PER_SECOND = 1000L;
    private static final int MAX_TIMEOUT = 5;

    private final ByteIngester ingester;
    private final JobDao jobDao;
    private final DocumentImageDao imageDao;
    private final DocumentTextDao textDao;

    private volatile boolean stopFlag = false;

    private final AtomicInteger activeThreadCount = new AtomicInteger(0);
    private ExecutorService executor = Executors.newCachedThreadPool();
    private CountDownLatch delegateLatch = new CountDownLatch(1);


    @Autowired
    public JobProcessor(JobDao jobDao, DocumentImageDao imageDao, ByteIngester ingester, DocumentTextDao textDao) {
        this.imageDao = imageDao;
        this.jobDao = jobDao;
        this.ingester = ingester;
        logger.info("JobProcessor spawning worker thread");
        spawnDelegateThread();
        this.textDao = textDao;
    }

    /**
     * Spawn thread that will query the database for new jobs and submit them to the executor service.
     */
    private void spawnDelegateThread() {
        new Thread(() -> {
            int noOpIterations = 0;
            while (!stopFlag) {
                boolean tooManyThreads;

                // This method may allow slightly more than JOB_QUEUE_SIZE threads to be created, but it should be
                // small enough that it does not matter.
                tooManyThreads = activeThreadCount.get() >= JOB_QUEUE_SIZE;

                // TODO: If this grows too big (more than some config value) trigger a warning.
                if (tooManyThreads) {
                    noOpIterations++;
                    sleep(noOpIterations);
                    continue;
                }

                Optional<ProcessingJob> job = jobDao.acceptNextJob(InstanceConfig.INSTANCE_ID);
                logger.debug("Accepted job " + job.toString());

                if (!job.isPresent()) {
                    // This should reduce load on both the server and the database when few/no jobs are available
                    noOpIterations++;
                    sleep(noOpIterations);
                } else {
                    noOpIterations = 0;

                    activeThreadCount.incrementAndGet();
                    executor.submit(() -> {
                        switch (job.get().getJobType()) {
                            case IMAGE: processImageJob(job.get()); break;
                            case FINGERPRINT: processDocumentJob(job.get()); break;
                        }
                    });
                }

            }
            delegateLatch.countDown();
            logger.info("JobProcessor worker thread exited");
        }).start();
    }

    private void processDocumentJob(ProcessingJob job) {

    }

    /**
     * Process an image.
     */
    private void processImageJob(@NonNull ProcessingJob job) {
        logger.debug("Processing job " + job);
        DocumentImage image = getImageFor(job);
        String rawText = ingester.ingest(image.toBufferedImage());
        DocumentText text = new DocumentText(rawText, image.getId());
        textDao.addDocumentText(text);

        jobDao.completeJob(job);
        activeThreadCount.decrementAndGet();
    }

    /**
     * Get the image from the database for a specific job, and convert it to a {@link BufferedImage}
     */
    @NonNull
    private DocumentImage getImageFor(ProcessingJob job) {
        Optional<DocumentImage> image = imageDao.getImageFor(job);
        if (!image.isPresent()) {
            // This should not happen
            logger.error("Could not find matching image for job " + job);
            throw new IllegalStateException("Could not load image from the database for job " + job);
        }
        return image.get();
    }

    private void sleep(int iteration) {
        long ms = Math.min(MAX_TIMEOUT, iteration) * MS_PER_SECOND;
        try {
            logger.debug("Job processor pausing for " + ms + " ms");
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.error("Thread timeout interrupted", e);
        }
    }

    /**
     * This method stops the job processor. Any job being processed at the time this method is called will be fully completed.
     * This method will block until all active jobs are completed.
     */
    public void stop() throws InterruptedException {
        logger.info("Setting flag to stop job processor");
        stopFlag = true;
        executor.shutdown();
        delegateLatch.await();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }


}
