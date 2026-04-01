package pitchmarketplace.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    @Around("execution(public * pitchmarketplace.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("Service method {} executed in {} ms", joinPoint.getSignature().toShortString(), durationMs);
            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.warn(
                    "Service method {} failed in {} ms: {}",
                    joinPoint.getSignature().toShortString(),
                    durationMs,
                    ex.getMessage()
            );
            throw ex;
        }
    }
}
