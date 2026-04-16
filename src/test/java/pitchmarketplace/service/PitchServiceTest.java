package pitchmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.dto.PitchUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.PitchRepository;

@ExtendWith(MockitoExtension.class)
class PitchServiceTest {

    @Mock
    private PitchRepository repository;

    private PitchService pitchService;
    private TrackingBookingSearchService bookingSearchService;

    @BeforeEach
    void setUp() {
        bookingSearchService = new TrackingBookingSearchService();
        pitchService = new PitchService(repository, bookingSearchService);
    }

    @Test
    void shouldFindAllPitchesWhenDistrictIsNull() {
        when(repository.findAll()).thenReturn(List.of(
                new Pitch(1L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN)
        ));

        assertThat(pitchService.findAll(null))
                .extracting(PitchDto::id, PitchDto::district)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, "Central"));
    }

    @Test
    void shouldFindAllPitchesWhenDistrictIsBlank() {
        when(repository.findAll()).thenReturn(List.of(
                new Pitch(1L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN)
        ));

        assertThat(pitchService.findAll("   ")).hasSize(1);
    }

    @Test
    void shouldFindPitchesByTrimmedDistrict() {
        when(repository.findByDistrictIgnoreCase("Central")).thenReturn(List.of(
                new Pitch(1L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN)
        ));

        assertThat(pitchService.findAll("  Central  "))
                .extracting(PitchDto::name)
                .containsExactly("Arena");
    }

    @Test
    void shouldThrowWhenPitchIsMissing() {
        when(repository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pitchService.findById(44L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pitch not found. id=44");
    }

    @Test
    void shouldCreatePitchAndInvalidateCache() {
        PitchUpsertRequest request = new PitchUpsertRequest(
                "Arena",
                PitchType.EIGHT,
                "Central",
                "Nemiga",
                new BigDecimal("120.00")
        );
        when(repository.save(org.mockito.ArgumentMatchers.any(Pitch.class))).thenAnswer(invocation -> {
            Pitch pitch = invocation.getArgument(0);
            pitch.setId(5L);
            return pitch;
        });

        PitchDto created = pitchService.create(request);

        assertThat(created.id()).isEqualTo(5L);
        assertThat(created.name()).isEqualTo("Arena");
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldUpdatePitchAndInvalidateCache() {
        Pitch existing = new Pitch(5L, "Old", PitchType.FIVE_TURF, "Old", "Old", BigDecimal.ONE);
        PitchUpsertRequest request = new PitchUpsertRequest(
                "New",
                PitchType.ELEVEN,
                "New District",
                "Mogilevskaya",
                new BigDecimal("180.00")
        );

        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        PitchDto updated = pitchService.update(5L, request);

        assertThat(updated.id()).isEqualTo(5L);
        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.type()).isEqualTo(PitchType.ELEVEN);
        assertThat(updated.district()).isEqualTo("New District");
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    @Test
    void shouldDeletePitchAndInvalidateCache() {
        Pitch existing = new Pitch(5L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));

        pitchService.delete(5L);

        verify(repository).delete(existing);
        verify(repository).flush();
        assertThat(bookingSearchService.invalidated).isTrue();
    }

    private static final class TrackingBookingSearchService extends BookingSearchService {

        private boolean invalidated;

        private TrackingBookingSearchService() {
            super(org.mockito.Mockito.mock(pitchmarketplace.repository.BookingRepository.class));
        }

        @Override
        public void invalidateCache() {
            invalidated = true;
        }
    }
}
