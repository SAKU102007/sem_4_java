package pitchmarketplace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.TimeZone;
import java.util.concurrent.Executor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pitchmarketplace.aspect.ServiceExecutionLoggingAspect;
import pitchmarketplace.config.AsyncConfig;

class InfrastructureCoverageTest {

    @Test
    void shouldLogServiceExecutionOnSuccess() throws Throwable {
        ServiceExecutionLoggingAspect aspect = new ServiceExecutionLoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("UserService.findAll()");
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.logExecutionTime(joinPoint);

        assertThat(result).isEqualTo("ok");
    }

    @Test
    void shouldLogServiceExecutionOnFailure() throws Throwable {
        ServiceExecutionLoggingAspect aspect = new ServiceExecutionLoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("UserService.findAll()");
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.logExecutionTime(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void shouldInstantiateApplicationAndRunMain() {
        PitchMarketplaceApplication application = new PitchMarketplaceApplication();
        TimeZone original = TimeZone.getDefault();

        assertThat(application).isNotNull();
        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            PitchMarketplaceApplication.main(new String[]{"--demo"});

            springApplication.verify(() -> SpringApplication.run(
                    PitchMarketplaceApplication.class,
                    new String[]{"--demo"}
            ));
            assertThat(TimeZone.getDefault().getID()).isEqualTo("Europe/Minsk");
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void shouldCreateAsyncExecutorBean() {
        AsyncConfig asyncConfig = new AsyncConfig();

        Executor executor = asyncConfig.concurrencyTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(threadPoolTaskExecutor.getThreadNamePrefix()).isEqualTo("pitch-report-");
        threadPoolTaskExecutor.shutdown();
    }
}
