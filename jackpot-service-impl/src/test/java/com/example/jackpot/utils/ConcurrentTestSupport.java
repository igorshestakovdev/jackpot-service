package com.example.jackpot.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

/**
 * Runs concurrent actions from the same start signal to make race-sensitive tests stable.
 */
public final class ConcurrentTestSupport {

    private ConcurrentTestSupport() {
    }

    /**
     * Starts tasks from a shared latch so concurrency scenarios execute at the same moment.
     */
    public static <T> List<Future<T>> submitConcurrently(ExecutorService executor, int taskCount,
                                                         IntFunction<Callable<T>> taskFactory) throws InterruptedException {

        CountDownLatch ready = new CountDownLatch(taskCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<T>> futures = new ArrayList<>();

        for (int index = 0; index < taskCount; index++) {

            int taskIndex = index;

            futures
                    .add(executor.submit(() -> {
                        ready.countDown();
                        await(start);
                        return taskFactory.apply(taskIndex).call();
                    }));
        }

        if (!ready.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out while waiting for concurrent test workers");
        }

        start.countDown();
        return futures;
    }

    /**
     * Waits for a latch and converts interruptions into test-friendly failures.
     */
    public static void await(CountDownLatch latch) {

        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrent test was interrupted", exception);
        }
    }
}
