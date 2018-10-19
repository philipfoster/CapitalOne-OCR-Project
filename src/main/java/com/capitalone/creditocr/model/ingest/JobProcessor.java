package com.capitalone.creditocr.model.ingest;

import com.capitalone.creditocr.conf.InstanceConfig;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
public class JobProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobProcessor.class);

    // Set max queue size to the number of CPU cores on the machine
    // TODO: Make this configurable.
    private static final int JOB_QUEUE_SIZE = Runtime.getRuntime().availableProcessors();
    private static final long MS_PER_SECOND = 1000L;
    private static final int MAX_TIMEOUT = 5;


//    private final ByteIngester ingester;
    private final JobDao jobDao;

    private volatile boolean stopFlag = false;

    private volatile int activeThreadCount = 0;
    private final Object COUNT_LOCK = new Object();

    private ExecutorService executor = Executors.newCachedThreadPool();
    private CountDownLatch delegateLatch = new CountDownLatch(1);


    @Autowired
    public JobProcessor(JobDao jobDao) {
//        this.ingester = ingester;
        logger.info("JobProcessor spawning worker thread");
        spawnDelegateThread();
        this.jobDao = jobDao;
    }

    /**
     * Spawn thread that will query the database for new jobs and submit them to the executor service.
     */
    private void spawnDelegateThread() {
        // This needs to be on a separate thread so that we don't block the constructor from completing.
        new Thread(() -> {
            // The number of times in a row that there was no job to grab.
            int noOpIterations = 0;
            while (!stopFlag) {
                boolean tooManyThreads;

                // This method may allow slightly more than JOB_QUEUE_SIZE threads to be created, but it should be
                // small enough that it does not matter.
                synchronized (COUNT_LOCK) {
                    tooManyThreads = activeThreadCount >= JOB_QUEUE_SIZE;
                }

                // Don't allow the thread pool to grow too large. This will cause processing times to grow for every task
                // as it will take longer for a thread to get its turn on the CPU.
                // If this grows too large, it may mean that more instances are needed.
                // TODO: If this grows too big (more than some config value) trigger a warning.
                if (tooManyThreads) {
                    noOpIterations++;
                    sleep(noOpIterations);
                    continue;
                }

                Optional<ProcessingJob> job = jobDao.acceptNextJob(InstanceConfig.INSTANCE_ID);

                if (!job.isPresent()) {
                    // This should reduce load on both the server and the database when few/no jobs are available
                    noOpIterations++;
                    sleep(noOpIterations);
                } else {
                    noOpIterations = 0;

                    // Submit the processing job the the thread pool
                    synchronized (COUNT_LOCK) {
                        activeThreadCount++;
                    }
                    executor.submit(() -> processJob(job.get()));
                }

            }
            delegateLatch.countDown();
            logger.info("JobProcessor worker threqgad exited");
        }).start();
    }

    private void processJob(@NonNull ProcessingJob job) {

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
