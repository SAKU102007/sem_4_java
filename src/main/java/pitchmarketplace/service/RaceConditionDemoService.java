package pitchmarketplace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import pitchmarketplace.dto.RaceConditionDemoRequest;
import pitchmarketplace.dto.RaceConditionDemoResultDto;

@Service
public class RaceConditionDemoService {

    public RaceConditionDemoResultDto demonstrate(RaceConditionDemoRequest request) {
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SafeCounter safeCounter = new AtomicSafeCounter();
        // SafeCounter safeCounter = new SynchronizedCounter();

        int threads = request.threads();
        int incrementsPerThread = request.incrementsPerThread();
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        List<Runnable> tasks = new ArrayList<>();

        for (int thread = 0; thread < threads; thread++) {
            tasks.add(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < incrementsPerThread; i++) {
                        unsafeCounter.increment();
                        safeCounter.increment();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        try {
            tasks.forEach(executorService::submit);
            startLatch.countDown();
            doneLatch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", ex);
        } finally {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        long expected = (long) threads * incrementsPerThread;
        long unsafeValue = unsafeCounter.get();
        return new RaceConditionDemoResultDto(
                threads,
                incrementsPerThread,
                expected,
                unsafeValue,
                safeCounter.get(),
                expected - unsafeValue
        );
    }

    private interface SafeCounter {

        void increment();

        long get();
    }

    private static final class AtomicSafeCounter implements SafeCounter {

        private final AtomicLong value = new AtomicLong();

        @Override
        public void increment() {
            value.incrementAndGet();
        }

        @Override
        public long get() {
            return value.get();
        }
    }

    private static final class SynchronizedCounter implements SafeCounter {

        private long value;

        @Override
        public synchronized void increment() {
            value++;
        }

        @Override
        public synchronized long get() {
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
