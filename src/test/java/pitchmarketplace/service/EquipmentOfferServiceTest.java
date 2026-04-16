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
import pitchmarketplace.domain.entity.EquipmentOffer;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.enums.EquipmentItemType;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.dto.EquipmentOfferDto;
import pitchmarketplace.dto.EquipmentOfferUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.repository.EquipmentOfferRepository;
import pitchmarketplace.repository.PitchRepository;

@ExtendWith(MockitoExtension.class)
class EquipmentOfferServiceTest {

    @Mock
    private EquipmentOfferRepository equipmentOfferRepository;

    @Mock
    private PitchRepository pitchRepository;

    private EquipmentOfferService service;

    @BeforeEach
    void setUp() {
        service = new EquipmentOfferService(equipmentOfferRepository, pitchRepository);
    }

    @Test
    void shouldFindAllEquipmentOffers() {
        Pitch pitch = new Pitch(2L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN);
        when(equipmentOfferRepository.findAll()).thenReturn(List.of(
                new EquipmentOffer(1L, pitch, EquipmentItemType.BALL, 10, new BigDecimal("15.00"))
        ));

        assertThat(service.findAll())
                .extracting(EquipmentOfferDto::id, EquipmentOfferDto::pitchId, EquipmentOfferDto::itemType)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, 2L, EquipmentItemType.BALL));
    }

    @Test
    void shouldFindEquipmentOfferById() {
        Pitch pitch = new Pitch(2L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN);
        when(equipmentOfferRepository.findById(1L)).thenReturn(Optional.of(
                new EquipmentOffer(1L, pitch, EquipmentItemType.BALL, 10, new BigDecimal("15.00"))
        ));

        EquipmentOfferDto result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.pitchId()).isEqualTo(2L);
        assertThat(result.itemType()).isEqualTo(EquipmentItemType.BALL);
    }

    @Test
    void shouldThrowWhenEquipmentOfferIsMissing() {
        when(equipmentOfferRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Equipment offer not found. id=9");
    }

    @Test
    void shouldCreateEquipmentOffer() {
        Pitch pitch = new Pitch(2L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN);
        EquipmentOfferUpsertRequest request = new EquipmentOfferUpsertRequest(
                2L,
                EquipmentItemType.BIBS,
                12,
                new BigDecimal("8.00")
        );

        when(pitchRepository.findById(2L)).thenReturn(Optional.of(pitch));
        when(equipmentOfferRepository.save(org.mockito.ArgumentMatchers.any(EquipmentOffer.class))).thenAnswer(invocation -> {
            EquipmentOffer offer = invocation.getArgument(0);
            offer.setId(6L);
            return offer;
        });

        EquipmentOfferDto created = service.create(request);

        assertThat(created.id()).isEqualTo(6L);
        assertThat(created.pitchId()).isEqualTo(2L);
        assertThat(created.itemType()).isEqualTo(EquipmentItemType.BIBS);
        assertThat(created.stockTotal()).isEqualTo(12);
    }

    @Test
    void shouldUpdateEquipmentOffer() {
        Pitch pitch = new Pitch(2L, "Arena", PitchType.EIGHT, "Central", "Nemiga", BigDecimal.TEN);
        EquipmentOffer existing = new EquipmentOffer(
                6L,
                pitch,
                EquipmentItemType.BALL,
                2,
                new BigDecimal("4.00")
        );
        EquipmentOfferUpsertRequest request = new EquipmentOfferUpsertRequest(
                2L,
                EquipmentItemType.BIBS,
                12,
                new BigDecimal("8.00")
        );

        when(equipmentOfferRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(pitchRepository.findById(2L)).thenReturn(Optional.of(pitch));
        when(equipmentOfferRepository.save(existing)).thenReturn(existing);

        EquipmentOfferDto updated = service.update(6L, request);

        assertThat(updated.id()).isEqualTo(6L);
        assertThat(updated.itemType()).isEqualTo(EquipmentItemType.BIBS);
        assertThat(updated.stockTotal()).isEqualTo(12);
        assertThat(updated.rentFixedPrice()).isEqualTo(new BigDecimal("8.00"));
    }

    @Test
    void shouldDeleteEquipmentOffer() {
        EquipmentOffer existing = new EquipmentOffer();
        existing.setId(6L);
        when(equipmentOfferRepository.findById(6L)).thenReturn(Optional.of(existing));

        service.delete(6L);

        verify(equipmentOfferRepository).delete(existing);
    }

    @Test
    void shouldThrowWhenPitchIsMissingDuringOfferCreate() {
        when(pitchRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new EquipmentOfferUpsertRequest(
                2L,
                EquipmentItemType.BIBS,
                12,
                new BigDecimal("8.00")
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pitch not found. id=2");
    }
}
