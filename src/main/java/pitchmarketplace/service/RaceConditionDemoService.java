package pitchmarketplace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;
import pitchmarketplace.dto.RaceConditionDemoRequest;
import pitchmarketplace.dto.RaceConditionDemoResultDto;

@Service
public class RaceConditionDemoService {

    public RaceConditionDemoResultDto demonstrate(RaceConditionDemoRequest request) {
        int threads = request.threads();
        int incrementsPerThread = request.incrementsPerThread();
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        runIncrementScenario(threads, incrementsPerThread, unsafeCounter::increment);

        AtomicLong atomicCounter = new AtomicLong();
        // SynchronizedCounter safeCounter = new SynchronizedCounter();
        runIncrementScenario(threads, incrementsPerThread, atomicCounter::incrementAndGet);
        // runIncrementScenario(threads, incrementsPerThread, safeCounter::increment);

        long expected = (long) threads * incrementsPerThread;
        long unsafeValue = unsafeCounter.get();
        long safeValue = atomicCounter.get();
        // long safeValue = safeCounter.get();
        return new RaceConditionDemoResultDto(
                threads,
                incrementsPerThread,
                expected,
                unsafeValue,
                safeValue,
                expected - unsafeValue
        );
    }

    private void runIncrementScenario(int threads, int incrementsPerThread, Runnable incrementAction) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicReference<RuntimeException> taskFailure = new AtomicReference<>();
        List<Runnable> tasks = new ArrayList<>();

        for (int thread = 0; thread < threads; thread++) {
            tasks.add(() -> {
                try {
                    readyLatch.countDown();
                    awaitLatch(startLatch);
                    for (int i = 0; i < incrementsPerThread; i++) {
                        incrementAction.run();
                    }
                } catch (RuntimeException ex) {
                    taskFailure.compareAndSet(null, ex);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        RuntimeException primaryFailure = null;
        try {
            tasks.forEach(executorService::submit);
            awaitLatch(readyLatch);
            startLatch.countDown();
            awaitLatch(doneLatch);
        } catch (RuntimeException ex) {
            primaryFailure = ex;
            throw ex;
        } finally {
            try {
                shutdownExecutor(executorService);
            } catch (RuntimeException ex) {
                if (primaryFailure == null) {
                    throw ex;
                }
                primaryFailure.addSuppressed(ex);
            }
        }

        RuntimeException failure = taskFailure.get();
        if (failure != null) {
            throw failure;
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", ex);
        }
    }

    private void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo shutdown was interrupted", ex);
        }
    }

    private static final class SynchronizedCounter {

        private long value;

        private synchronized void increment() {
            value++;
        }

        private synchronized long get() {
            return value;
        }
    }

    private static final class UnsafeCounter {

        private long value;

        private void increment() {
            value++;
        }

        private long get() {
            return value;
        }
    }

}
