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
        AtomicLong atomicCounter = new AtomicLong();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();

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
                        atomicCounter.incrementAndGet();
                        synchronizedCounter.increment();
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
                synchronizedCounter.get(),
                atomicCounter.get(),
                expected - unsafeValue
        );
    }

    private static final class UnsafeCounter {

        private long value;

        private void increment() {
            long snapshot = value;
            if ((snapshot & 15L) == 0L) {
                Thread.yield();
            }
            value = snapshot + 1;
        }

        private long get() {
            return value;
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
}
