package example.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConcurrentTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConcurrentTestUtils.class);

    public static <T> List<T> runConcurrentOperationsWithResults(int threadCount,
                                                                 int operationsPerThread,
                                                                 Supplier<T> operation) throws InterruptedException {

        // Use a thread-safe list to collect all results
        List<T> allResults = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);

        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // Submit all tasks
        for (int threadId = 0; threadId < threadCount; threadId++) {
            final int currentThreadId = threadId;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    logger.debug("Thread {} starting operations", currentThreadId);

                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            T result = operation.get();
                            if (result != null) {
                                allResults.add(result);
                                logger.debug("Thread {} added result {}. Total results so far: {}",
                                        currentThreadId, (j + 1), allResults.size());
                            } else {
                                logger.warn("Thread {} got null result for operation {}", currentThreadId, (j + 1));
                            }
                        } catch (Exception e) {
                            logger.error("Thread {} failed on operation {}: {}", currentThreadId, (j + 1), e.getMessage());
                            exceptions.add(e);
                        }
                    }

                    logger.debug("Thread {} completed all operations", currentThreadId);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exceptions.add(new RuntimeException("Thread " + currentThreadId + " interrupted", e));
                } catch (Exception e) {
                    exceptions.add(new RuntimeException("Thread " + currentThreadId + " failed", e));
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        logger.debug("Starting all {} threads", threadCount);
        startLatch.countDown();

        // Wait for all threads to complete
        boolean completed = completionLatch.await(60, TimeUnit.SECONDS);

        if (!completed) {
            executor.shutdownNow();
            throw new RuntimeException("Operations timed out after 60 seconds");
        }

        executor.shutdown();

        // Check for exceptions
        if (!exceptions.isEmpty()) {
            logger.error("Found {} exceptions:", exceptions.size());
            for (Exception e : exceptions) {
                logger.error("Exception details:", e);
            }
            throw new RuntimeException("Some operations failed. First exception: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }

        logger.debug("All threads completed. Final result count: {}", allResults.size());
        return new ArrayList<>(allResults); // Return a copy to avoid any concurrency issues
    }
}