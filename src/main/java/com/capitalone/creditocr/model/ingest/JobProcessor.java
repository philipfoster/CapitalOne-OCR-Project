package com.capitalone.creditocr.model.ingest;

import com.capitalone.creditocr.conf.InstanceConfig;
import com.capitalone.creditocr.model.dao.DocumentDao;
import com.capitalone.creditocr.model.dao.DocumentImageDao;
import com.capitalone.creditocr.model.dao.DocumentTextDao;
import com.capitalone.creditocr.model.dao.JobDao;
import com.capitalone.creditocr.model.dto.document.Document;
import com.capitalone.creditocr.model.dto.document.DocumentText;
import com.capitalone.creditocr.model.dto.document_image.DocumentImage;
import com.capitalone.creditocr.model.dto.job.ProcessingJob;
import com.capitalone.creditocr.util.Simhash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class JobProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobProcessor.class);

    private static final long MS_PER_SECOND = 1000L;

    @Value( "${processor.maxSleep}" )
    private int maxTimeout;

    @Value( "${processor.similarityThreshold}" )
    private float similarityThreshold;

    @Value( "${processor.maxWorkerThreads}" )
    private int jobQueueSize;


    private final ByteIngester ingester;
    private final JobDao jobDao;
    private final DocumentImageDao imageDao;
    private final DocumentTextDao textDao;
    private final DocumentDao documentDao;
    private final InfoExtractor infoExtractor;

    private volatile boolean stopFlag = false;

    private final AtomicInteger activeThreadCount = new AtomicInteger(0);
    private ExecutorService executor = Executors.newCachedThreadPool();
    private CountDownLatch delegateLatch = new CountDownLatch(1);

    @Autowired
    public JobProcessor(JobDao jobDao, DocumentImageDao imageDao, ByteIngester ingester, DocumentTextDao textDao,
                        DocumentDao documentDao, InfoExtractor infoExtractor) {
        this.imageDao = imageDao;
        this.jobDao = jobDao;
        this.ingester = ingester;
        this.textDao = textDao;
        this.documentDao = documentDao;
        this.infoExtractor = infoExtractor;

        logger.info("JobProcessor spawning worker thread");
        spawnDelegateThread();
    }


    /**
     * Spawn thread that will query the database for new jobs and submit them to the executor service.
     */
    private void spawnDelegateThread() {
        new Thread(() -> {
            int noOpIterations = 0;
            while (!stopFlag) {

                // This method may allow slightly more than jobQueueSize threads to be created, but it should be
                // small enough that it does not matter.
                var tooManyThreads = activeThreadCount.get() >= jobQueueSize;

                // TODO: If this grows too big (more than some config value) trigger a warning.
                if (tooManyThreads) {
                    noOpIterations++;
                    sleep(noOpIterations);
                    continue;
                }

                Optional<ProcessingJob> job = jobDao.acceptNextJob(InstanceConfig.INSTANCE_ID);
                logger.debug("Accepted job " + job.toString());

                if (job.isEmpty()) {
                    // This should reduce load on both the server and the database when few/no jobs are available
                    noOpIterations++;
                    sleep(noOpIterations);
                } else {
                    noOpIterations = 0;

                    activeThreadCount.incrementAndGet();

                    // This is getting into god object territory
                    // TODO: Refactor this into worker class and move specific processing logic to that class
                    executor.submit(() -> {
                        try {
                            switch (job.get().getJobType()) {
                                case IMAGE:
                                    processImageJob(job.get());
                                    break;
                                case FINGERPRINT:
                                    processDocumentJob(job.get());
                                    break;
                            }
                            jobDao.completeJob(job.get());
                        } catch (Exception e) {
                            logger.error("Job task failed. details: " + job, e);
                            // TODO: Mark job as error
                        } finally {
                            activeThreadCount.decrementAndGet();
                        }
                    });
                }

            }
            delegateLatch.countDown();
            logger.info("JobProcessor worker thread exited");
        }).start();
    }

    /**
     * Process a document
     */
    private void processDocumentJob(ProcessingJob job) {

        // Need to:
        // 1. Generate fingerprint
        // 2. Count similar documents
        // 2. Extract data
        // 3. Sort into queue
        Objects.requireNonNull(job.getDocumentFk());
        Optional<Document> documentOptional= documentDao.getDocumentById(job.getDocumentFk());
        Document document;

        if (documentOptional.isPresent()) {
            document = documentOptional.get();
        } else {
            // This should never happen.
            // TODO: Investigate how to mark a job as failed
            logger.error(String.format("Could not process job %s. document does not exist...", job));
            return;
        }

        String fullText = textDao.getFullDocumentText(job.getDocumentFk());

        // Generate fingerprint and count similar documents
        byte[] fingerprint = Simhash.hash(fullText);
        document.setFingerprint(fingerprint);
        int numSimilarDocuments = documentDao.getSimilarDocumentIds(fingerprint, similarityThreshold ).size();
        document.setNumSimilarDocuments(numSimilarDocuments);

        // Extract customer information from letters
        var letterData = new LetterData(fullText);
        infoExtractor.extractDate(letterData);
        infoExtractor.extractNumbers(letterData);
        infoExtractor.extractAddress(letterData);

        String ssn = letterData.getSSN();
        document.setSsn(((ssn == null) || ssn.isEmpty()) ? null : ssn);
        try {
            document.setAccountNumber(Long.parseLong(letterData.getAcctNum()));
        } catch (NumberFormatException e) {
            // At the moment, the database isn't set up to handle partial numbers...
            logger.debug("Got partial account number.", e);
        }
        document.setLetterDate(letterData.getLetterDate());

        var queueSorter = new LetterQueueProcessor();
        var normalizedText = queueSorter.normalizeLetterText(letterData);
        queueSorter.processText(normalizedText);
        String queue = queueSorter.getFinalQueue();

        logger.info(String.format("Document %s assigned to queue %s. Keyword = %s", document.getId(), queue, queueSorter.getMatchingDescriptions()));

        document.setQueue(queue);

        // Save document to database
        documentDao.updateDocument(document);

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
    }

    /**
     * Get the image from the database for a specific job, and convert it to a {@link BufferedImage}
     */
    @NonNull
    private DocumentImage getImageFor(ProcessingJob job) {
        Optional<DocumentImage> image = imageDao.getImageForJob(job);
        if (image.isEmpty()) {
            // This should not happen
            logger.error("Could not find matching image for job " + job);
            throw new IllegalStateException("Could not load image from the database for job " + job);
        }
        return image.get();
    }

    private void sleep(int iteration) {
        long ms = Math.min( maxTimeout, iteration) * MS_PER_SECOND;
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
