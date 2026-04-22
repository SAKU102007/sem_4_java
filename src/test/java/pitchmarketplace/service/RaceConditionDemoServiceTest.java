package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pitchmarketplace.dto.RaceConditionDemoRequest;

class RaceConditionDemoServiceTest {

    private RaceConditionDemoService service;

    @BeforeEach
    void setUp() {
        service = new RaceConditionDemoService();
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    @Test
    void shouldDemonstrateUnsafeAndSafeCounters() {
        var result = service.demonstrate(new RaceConditionDemoRequest(64, 2000));

        assertThat(result.expected()).isEqualTo(128000L);
        assertThat(result.safeCounter()).isEqualTo(128000L);
        assertThat(result.unsafeCounter()).isLessThan(result.expected());
        assertThat(result.unsafeLostUpdates()).isPositive();
    }

    @Test
    void shouldThrowWhenRaceConditionDemoIsInterrupted() {
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> service.demonstrate(new RaceConditionDemoRequest(50, 100)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Race condition demo was interrupted");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
