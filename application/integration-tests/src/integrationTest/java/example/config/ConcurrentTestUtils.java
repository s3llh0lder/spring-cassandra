package example.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentTestUtils.class);

    public static void runConcurrentOperations(int threadCount, int operationsPerThread,
                                               Runnable operation) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Future<?> future = executor.submit(() -> {
                try {
                    // Wait for all threads to be ready before starting
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        operation.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", e);
                } finally {
                    completionLatch.countDown();
                }
            });
            futures.add(future);
        }

        // Release all threads at once
        startLatch.countDown();

        // Wait for all threads to complete
        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Concurrent operations timed out");
        }

        // Check for any exceptions
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new RuntimeException("Concurrent operation failed", e.getCause());
            }
        }

        executor.shutdown();
    }

    public static <T> List<T> runConcurrentOperationsWithResults(int threadCount,
                                                                 int operationsPerThread,
                                                                 Supplier<T> operation) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<List<T>>> futures = new ArrayList<>();

        // Submit all tasks
        for (int i = 0; i < threadCount; i++) {
            Future<List<T>> future = executor.submit(() -> {
                List<T> results = new ArrayList<>();
                try {
                    // Wait for all threads to be ready before starting
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        T result = operation.get();
                        if (result != null) {
                            results.add(result);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", e);
                } catch (Exception e) {
                    throw new RuntimeException("Operation failed", e);
                }
                return results;
            });
            futures.add(future);
        }

        // Release all threads at once for true concurrency
        startLatch.countDown();

        // Collect all results with better error handling and debugging
        List<T> allResults = new ArrayList<>();
        int threadIndex = 0;
        for (Future<List<T>> future : futures) {
            try {
                List<T> threadResults = future.get(30, TimeUnit.SECONDS);
                logger.debug("Thread {} returned {} results", threadIndex, threadResults.size());
                if (threadResults != null) {
                    allResults.addAll(threadResults);
                }
                threadIndex++;
            } catch (ExecutionException e) {
                executor.shutdownNow();
                logger.debug("ExecutionException in thread {}: {}", threadIndex, e.getCause().getMessage());
                throw new RuntimeException("Concurrent operation failed", e.getCause());
            } catch (TimeoutException e) {
                executor.shutdownNow();
                logger.debug("TimeoutException in thread {}", threadIndex);
                throw new RuntimeException("Concurrent operation timed out", e);
            }
        }

        executor.shutdown();
        logger.debug("Total results collected: {}", allResults.size());
        return allResults;
    }
}