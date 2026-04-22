package pitchmarketplace;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pitchmarketplace.domain.entity.Booking;
import pitchmarketplace.domain.entity.EquipmentOffer;
import pitchmarketplace.domain.entity.OpenGame;
import pitchmarketplace.domain.entity.Pitch;
import pitchmarketplace.domain.entity.User;
import pitchmarketplace.domain.enums.BookingStatus;
import pitchmarketplace.domain.enums.EquipmentItemType;
import pitchmarketplace.domain.enums.OpenGameStatus;
import pitchmarketplace.domain.enums.PitchType;
import pitchmarketplace.domain.enums.UserRole;
import pitchmarketplace.dto.ApiErrorResponse;
import pitchmarketplace.dto.AsyncTaskAcceptedDto;
import pitchmarketplace.dto.AsyncTaskState;
import pitchmarketplace.dto.AsyncTaskStatusDto;
import pitchmarketplace.dto.BookingDto;
import pitchmarketplace.dto.BookingSearchCriteria;
import pitchmarketplace.dto.BookingSearchRequest;
import pitchmarketplace.dto.BookingSearchResponseDto;
import pitchmarketplace.dto.BookingUpsertRequest;
import pitchmarketplace.dto.BulkBookingTransactionDemoResultDto;
import pitchmarketplace.dto.ConcurrencyCounterStatsDto;
import pitchmarketplace.dto.EntityCountSnapshotDto;
import pitchmarketplace.dto.EquipmentOfferDto;
import pitchmarketplace.dto.EquipmentOfferUpsertRequest;
import pitchmarketplace.dto.NPlusOneDemoResultDto;
import pitchmarketplace.dto.OpenGameDto;
import pitchmarketplace.dto.OpenGameUpsertRequest;
import pitchmarketplace.dto.PitchLoadReportRequest;
import pitchmarketplace.dto.PitchLoadReportResultDto;
import pitchmarketplace.dto.PitchDto;
import pitchmarketplace.dto.PitchUpsertRequest;
import pitchmarketplace.dto.RaceConditionDemoRequest;
import pitchmarketplace.dto.RaceConditionDemoResultDto;
import pitchmarketplace.dto.TransactionDemoResultDto;
import pitchmarketplace.dto.UserDto;
import pitchmarketplace.dto.UserUpsertRequest;
import pitchmarketplace.exception.ResourceNotFoundException;
import pitchmarketplace.service.cache.BookingSearchCacheKey;

class ModelCoverageTest {

    @Test
    void shouldCoverEntityAccessorsAndRelationships() {
        Pitch pitch = new Pitch(1L, "Arena", PitchType.EIGHT, "Central", "Nemiga", new BigDecimal("120.00"));
        EquipmentOffer offer = new EquipmentOffer(2L, null, EquipmentItemType.BALL, 10, new BigDecimal("15.00"));
        User organizer = new User(3L, "Alexey", 78, UserRole.PLAYER);
        Booking booking = new Booking(
                4L,
                pitch,
                organizer,
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-01T20:00:00"),
                BookingStatus.CREATED
        );
        OpenGame openGame = new OpenGame(5L, booking, organizer, 40, 70, 12, OpenGameStatus.OPEN);

        pitch.addEquipmentOffer(offer);
        assertThat(pitch.getEquipmentOffers()).containsExactly(offer);
        assertThat(offer.getPitch()).isSameAs(pitch);
        pitch.removeEquipmentOffer(offer);
        assertThat(pitch.getEquipmentOffers()).isEmpty();
        assertThat(offer.getPitch()).isNull();

        openGame.addParticipant(organizer);
        booking.setOpenGame(openGame);
        assertThat(booking.getOpenGame()).isSameAs(openGame);
        assertThat(openGame.getParticipants()).containsExactly(organizer);

        pitch.setBookings(Set.of(booking));
        pitch.setEquipmentOffers(Set.of(offer));
        organizer.setOrganizedBookings(Set.of(booking));
        organizer.setOrganizedOpenGames(Set.of(openGame));
        organizer.setParticipatingOpenGames(Set.of(openGame));
        openGame.setParticipants(Set.of(organizer));

        assertThat(pitch.getId()).isEqualTo(1L);
        assertThat(pitch.getName()).isEqualTo("Arena");
        assertThat(pitch.getType()).isEqualTo(PitchType.EIGHT);
        assertThat(pitch.getDistrict()).isEqualTo("Central");
        assertThat(pitch.getMetro()).isEqualTo("Nemiga");
        assertThat(pitch.getPricePerHour()).isEqualTo(new BigDecimal("120.00"));
        assertThat(pitch.getBookings()).containsExactly(booking);
        assertThat(pitch.getEquipmentOffers()).containsExactly(offer);

        pitch.setId(11L);
        pitch.setName("New Arena");
        pitch.setType(PitchType.ELEVEN);
        pitch.setDistrict("New District");
        pitch.setMetro("New Metro");
        pitch.setPricePerHour(new BigDecimal("200.00"));
        assertThat(pitch.getId()).isEqualTo(11L);
        assertThat(pitch.getName()).isEqualTo("New Arena");
        assertThat(pitch.getType()).isEqualTo(PitchType.ELEVEN);
        assertThat(pitch.getDistrict()).isEqualTo("New District");
        assertThat(pitch.getMetro()).isEqualTo("New Metro");
        assertThat(pitch.getPricePerHour()).isEqualTo(new BigDecimal("200.00"));

        assertThat(organizer.getId()).isEqualTo(3L);
        assertThat(organizer.getName()).isEqualTo("Alexey");
        assertThat(organizer.getRating()).isEqualTo(78);
        assertThat(organizer.getRole()).isEqualTo(UserRole.PLAYER);
        assertThat(organizer.getOrganizedBookings()).containsExactly(booking);
        assertThat(organizer.getOrganizedOpenGames()).containsExactly(openGame);
        assertThat(organizer.getParticipatingOpenGames()).containsExactly(openGame);

        organizer.setId(13L);
        organizer.setName("Denis");
        organizer.setRating(60);
        organizer.setRole(UserRole.ADMIN);
        assertThat(organizer.getId()).isEqualTo(13L);
        assertThat(organizer.getName()).isEqualTo("Denis");
        assertThat(organizer.getRating()).isEqualTo(60);
        assertThat(organizer.getRole()).isEqualTo(UserRole.ADMIN);

        assertThat(offer.getId()).isEqualTo(2L);
        assertThat(offer.getItemType()).isEqualTo(EquipmentItemType.BALL);
        assertThat(offer.getStockTotal()).isEqualTo(10);
        assertThat(offer.getRentFixedPrice()).isEqualTo(new BigDecimal("15.00"));
        offer.setId(12L);
        offer.setPitch(pitch);
        offer.setItemType(EquipmentItemType.BIBS);
        offer.setStockTotal(15);
        offer.setRentFixedPrice(new BigDecimal("9.00"));
        assertThat(offer.getId()).isEqualTo(12L);
        assertThat(offer.getPitch()).isSameAs(pitch);
        assertThat(offer.getItemType()).isEqualTo(EquipmentItemType.BIBS);
        assertThat(offer.getStockTotal()).isEqualTo(15);
        assertThat(offer.getRentFixedPrice()).isEqualTo(new BigDecimal("9.00"));

        assertThat(booking.getId()).isEqualTo(4L);
        assertThat(booking.getPitch()).isSameAs(pitch);
        assertThat(booking.getOrganizer()).isSameAs(organizer);
        assertThat(booking.getStartAt()).isEqualTo(LocalDateTime.parse("2026-05-01T18:00:00"));
        assertThat(booking.getEndAt()).isEqualTo(LocalDateTime.parse("2026-05-01T20:00:00"));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CREATED);
        booking.setId(14L);
        booking.setPitch(pitch);
        booking.setOrganizer(organizer);
        booking.setStartAt(LocalDateTime.parse("2026-05-02T18:00:00"));
        booking.setEndAt(LocalDateTime.parse("2026-05-02T20:00:00"));
        booking.setStatus(BookingStatus.CONFIRMED);
        assertThat(booking.getId()).isEqualTo(14L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        assertThat(openGame.getId()).isEqualTo(5L);
        assertThat(openGame.getBooking()).isSameAs(booking);
        assertThat(openGame.getOrganizer()).isSameAs(organizer);
        assertThat(openGame.getTargetSkillMin()).isEqualTo(40);
        assertThat(openGame.getTargetSkillMax()).isEqualTo(70);
        assertThat(openGame.getMaxPlayers()).isEqualTo(12);
        assertThat(openGame.getStatus()).isEqualTo(OpenGameStatus.OPEN);
        openGame.setId(15L);
        openGame.setBooking(booking);
        openGame.setOrganizer(organizer);
        openGame.setTargetSkillMin(50);
        openGame.setTargetSkillMax(80);
        openGame.setMaxPlayers(14);
        openGame.setStatus(OpenGameStatus.FULL);
        assertThat(openGame.getId()).isEqualTo(15L);
        assertThat(openGame.getTargetSkillMin()).isEqualTo(50);
        assertThat(openGame.getTargetSkillMax()).isEqualTo(80);
        assertThat(openGame.getMaxPlayers()).isEqualTo(14);
        assertThat(openGame.getStatus()).isEqualTo(OpenGameStatus.FULL);
    }

    @Test
    void shouldCoverDtoRecordsAndEnums() {
        Instant now = Instant.now();
        LocalDateTime start = LocalDateTime.parse("2026-05-01T18:00:00");
        LocalDateTime end = LocalDateTime.parse("2026-05-01T20:00:00");
        EntityCountSnapshotDto snapshot = new EntityCountSnapshotDto(1, 2, 3, 4, 5);
        BookingDto bookingDto = new BookingDto(1L, 2L, 3L, start, end, BookingStatus.CREATED);
        BookingSearchResponseDto searchResponse = new BookingSearchResponseDto(
                "jpql",
                false,
                0,
                5,
                1,
                1,
                true,
                true,
                List.of(bookingDto)
        );

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(now, 400, "Bad Request", "Validation failed",
                "/api/v1/pitches", List.of("name: required"));
        BulkBookingTransactionDemoResultDto bulk = new BulkBookingTransactionDemoResultDto(
                "bulk_with_transaction",
                2,
                "error",
                snapshot,
                snapshot
        );
        AsyncTaskAcceptedDto acceptedTask = new AsyncTaskAcceptedDto(7L, AsyncTaskState.ACCEPTED, now);
        PitchLoadReportResultDto pitchLoadReport = new PitchLoadReportResultDto(2L, "Arena", 8, 5, 1, 3, 2);
        AsyncTaskStatusDto asyncTaskStatus = new AsyncTaskStatusDto(
                7L,
                AsyncTaskState.COMPLETED,
                now,
                now,
                now,
                null,
                pitchLoadReport
        );
        ConcurrencyCounterStatsDto counterStats = new ConcurrencyCounterStatsDto(7L, 7L, 6L, 1L);
        EquipmentOfferDto equipmentOfferDto = new EquipmentOfferDto(
                4L,
                2L,
                EquipmentItemType.BALL,
                10,
                new BigDecimal("15.00")
        );
        EquipmentOfferUpsertRequest equipmentOfferUpsertRequest = new EquipmentOfferUpsertRequest(
                2L,
                EquipmentItemType.BIBS,
                12,
                new BigDecimal("8.00")
        );
        NPlusOneDemoResultDto nPlusOne = new NPlusOneDemoResultDto("bad", 3, 4, 5);
        OpenGameDto openGameDto = new OpenGameDto(5L, 1L, 3L, 40, 70, 12, OpenGameStatus.OPEN, List.of(3L, 4L));
        PitchDto pitchDto = new PitchDto(2L, "Arena", PitchType.EIGHT, "Central", "Nemiga", new BigDecimal("120.00"));
        PitchLoadReportRequest pitchLoadReportRequest = new PitchLoadReportRequest(2L);
        PitchUpsertRequest pitchUpsertRequest = new PitchUpsertRequest(
                "Arena",
                PitchType.EIGHT,
                "Central",
                "Nemiga",
                new BigDecimal("120.00")
        );
        RaceConditionDemoRequest raceConditionDemoRequest = new RaceConditionDemoRequest(64, 2000);
        RaceConditionDemoResultDto raceConditionDemoResult = new RaceConditionDemoResultDto(
                64,
                2000,
                128000L,
                100000L,
                128000L,
                28000L
        );
        TransactionDemoResultDto transaction = new TransactionDemoResultDto("with_transaction", "error", snapshot, snapshot);
        UserDto userDto = new UserDto(3L, "Alexey", 78, UserRole.PLAYER);
        UserUpsertRequest userUpsertRequest = new UserUpsertRequest("Alexey", 78, UserRole.PLAYER);
        ResourceNotFoundException notFound = new ResourceNotFoundException("missing");

        assertThat(apiErrorResponse.timestamp()).isEqualTo(now);
        assertThat(apiErrorResponse.status()).isEqualTo(400);
        assertThat(apiErrorResponse.error()).isEqualTo("Bad Request");
        assertThat(apiErrorResponse.message()).isEqualTo("Validation failed");
        assertThat(apiErrorResponse.path()).isEqualTo("/api/v1/pitches");
        assertThat(apiErrorResponse.details()).containsExactly("name: required");
        assertThat(bookingDto.id()).isEqualTo(1L);
        assertThat(bookingDto.pitchId()).isEqualTo(2L);
        assertThat(bookingDto.organizerId()).isEqualTo(3L);
        assertThat(bookingDto.startAt()).isEqualTo(start);
        assertThat(bookingDto.endAt()).isEqualTo(end);
        assertThat(bookingDto.status()).isEqualTo(BookingStatus.CREATED);
        assertThat(searchResponse.markCacheHit().cacheHit()).isTrue();
        assertThat(searchResponse.queryType()).isEqualTo("jpql");
        assertThat(searchResponse.pageNumber()).isZero();
        assertThat(searchResponse.pageSize()).isEqualTo(5);
        assertThat(searchResponse.totalElements()).isEqualTo(1);
        assertThat(searchResponse.totalPages()).isEqualTo(1);
        assertThat(searchResponse.first()).isTrue();
        assertThat(searchResponse.last()).isTrue();
        assertThat(searchResponse.content()).containsExactly(bookingDto);
        assertThat(bulk.mode()).isEqualTo("bulk_with_transaction");
        assertThat(bulk.requestedItems()).isEqualTo(2);
        assertThat(bulk.error()).isEqualTo("error");
        assertThat(bulk.before()).isEqualTo(snapshot);
        assertThat(bulk.after()).isEqualTo(snapshot);
        assertThat(acceptedTask.taskId()).isEqualTo(7L);
        assertThat(acceptedTask.status()).isEqualTo(AsyncTaskState.ACCEPTED);
        assertThat(acceptedTask.createdAt()).isEqualTo(now);
        assertThat(asyncTaskStatus.taskId()).isEqualTo(7L);
        assertThat(asyncTaskStatus.status()).isEqualTo(AsyncTaskState.COMPLETED);
        assertThat(asyncTaskStatus.result()).isEqualTo(pitchLoadReport);
        assertThat(counterStats.lastIssuedTaskId()).isEqualTo(7L);
        assertThat(counterStats.submittedTasks()).isEqualTo(7L);
        assertThat(counterStats.completedTasks()).isEqualTo(6L);
        assertThat(counterStats.failedTasks()).isEqualTo(1L);
        assertThat(snapshot.users()).isEqualTo(1);
        assertThat(snapshot.pitches()).isEqualTo(2);
        assertThat(snapshot.bookings()).isEqualTo(3);
        assertThat(snapshot.openGames()).isEqualTo(4);
        assertThat(snapshot.equipmentOffers()).isEqualTo(5);
        assertThat(equipmentOfferDto.id()).isEqualTo(4L);
        assertThat(equipmentOfferDto.pitchId()).isEqualTo(2L);
        assertThat(equipmentOfferDto.itemType()).isEqualTo(EquipmentItemType.BALL);
        assertThat(equipmentOfferDto.stockTotal()).isEqualTo(10);
        assertThat(equipmentOfferDto.rentFixedPrice()).isEqualTo(new BigDecimal("15.00"));
        assertThat(equipmentOfferUpsertRequest.pitchId()).isEqualTo(2L);
        assertThat(equipmentOfferUpsertRequest.itemType()).isEqualTo(EquipmentItemType.BIBS);
        assertThat(equipmentOfferUpsertRequest.stockTotal()).isEqualTo(12);
        assertThat(equipmentOfferUpsertRequest.rentFixedPrice()).isEqualTo(new BigDecimal("8.00"));
        assertThat(nPlusOne.mode()).isEqualTo("bad");
        assertThat(nPlusOne.openGamesCount()).isEqualTo(3);
        assertThat(nPlusOne.totalParticipants()).isEqualTo(4);
        assertThat(nPlusOne.executedStatements()).isEqualTo(5);
        assertThat(openGameDto.id()).isEqualTo(5L);
        assertThat(openGameDto.bookingId()).isEqualTo(1L);
        assertThat(openGameDto.organizerId()).isEqualTo(3L);
        assertThat(openGameDto.targetSkillMin()).isEqualTo(40);
        assertThat(openGameDto.targetSkillMax()).isEqualTo(70);
        assertThat(openGameDto.maxPlayers()).isEqualTo(12);
        assertThat(openGameDto.status()).isEqualTo(OpenGameStatus.OPEN);
        assertThat(openGameDto.participantIds()).containsExactly(3L, 4L);
        assertThat(pitchDto.id()).isEqualTo(2L);
        assertThat(pitchDto.name()).isEqualTo("Arena");
        assertThat(pitchDto.type()).isEqualTo(PitchType.EIGHT);
        assertThat(pitchDto.district()).isEqualTo("Central");
        assertThat(pitchDto.metro()).isEqualTo("Nemiga");
        assertThat(pitchDto.pricePerHour()).isEqualTo(new BigDecimal("120.00"));
        assertThat(pitchLoadReportRequest.pitchId()).isEqualTo(2L);
        assertThat(pitchLoadReport.pitchName()).isEqualTo("Arena");
        assertThat(pitchLoadReport.totalBookings()).isEqualTo(8L);
        assertThat(pitchLoadReport.confirmedBookings()).isEqualTo(5L);
        assertThat(pitchLoadReport.cancelledBookings()).isEqualTo(1L);
        assertThat(pitchLoadReport.totalOpenGames()).isEqualTo(3L);
        assertThat(pitchLoadReport.openOpenGames()).isEqualTo(2L);
        assertThat(pitchUpsertRequest.name()).isEqualTo("Arena");
        assertThat(pitchUpsertRequest.type()).isEqualTo(PitchType.EIGHT);
        assertThat(pitchUpsertRequest.district()).isEqualTo("Central");
        assertThat(pitchUpsertRequest.metro()).isEqualTo("Nemiga");
        assertThat(pitchUpsertRequest.pricePerHour()).isEqualTo(new BigDecimal("120.00"));
        assertThat(raceConditionDemoRequest.threads()).isEqualTo(64);
        assertThat(raceConditionDemoRequest.incrementsPerThread()).isEqualTo(2000);
        assertThat(raceConditionDemoResult.expected()).isEqualTo(128000L);
        assertThat(raceConditionDemoResult.unsafeCounter()).isEqualTo(100000L);
        assertThat(raceConditionDemoResult.safeCounter()).isEqualTo(128000L);
        assertThat(raceConditionDemoResult.unsafeLostUpdates()).isEqualTo(28000L);
        assertThat(transaction.mode()).isEqualTo("with_transaction");
        assertThat(transaction.error()).isEqualTo("error");
        assertThat(transaction.before()).isEqualTo(snapshot);
        assertThat(transaction.after()).isEqualTo(snapshot);
        assertThat(userDto.id()).isEqualTo(3L);
        assertThat(userDto.name()).isEqualTo("Alexey");
        assertThat(userDto.rating()).isEqualTo(78);
        assertThat(userDto.role()).isEqualTo(UserRole.PLAYER);
        assertThat(userUpsertRequest.name()).isEqualTo("Alexey");
        assertThat(userUpsertRequest.rating()).isEqualTo(78);
        assertThat(userUpsertRequest.role()).isEqualTo(UserRole.PLAYER);
        assertThat(notFound).hasMessage("missing");

        assertThat(BookingStatus.valueOf("CREATED")).isEqualTo(BookingStatus.CREATED);
        assertThat(EquipmentItemType.valueOf("BALL")).isEqualTo(EquipmentItemType.BALL);
        assertThat(OpenGameStatus.valueOf("OPEN")).isEqualTo(OpenGameStatus.OPEN);
        assertThat(PitchType.valueOf("EIGHT")).isEqualTo(PitchType.EIGHT);
        assertThat(UserRole.valueOf("ADMIN")).isEqualTo(UserRole.ADMIN);
        assertThat(AsyncTaskState.valueOf("COMPLETED")).isEqualTo(AsyncTaskState.COMPLETED);
        assertThat(BookingStatus.values()).containsExactly(
                BookingStatus.CREATED,
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED
        );
    }

    @Test
    void shouldCoverValidationHelpersAndCriteriaNormalization() {
        BookingUpsertRequest validBookingRequest = new BookingUpsertRequest(
                1L,
                2L,
                LocalDateTime.parse("2026-05-01T18:00:00"),
                LocalDateTime.parse("2026-05-01T20:00:00"),
                BookingStatus.CREATED
        );
        BookingUpsertRequest invalidBookingRequest = new BookingUpsertRequest(
                1L,
                2L,
                LocalDateTime.parse("2026-05-01T20:00:00"),
                LocalDateTime.parse("2026-05-01T18:00:00"),
                BookingStatus.CREATED
        );
        BookingUpsertRequest nullableBookingRequest = new BookingUpsertRequest(
                1L,
                2L,
                null,
                null,
                BookingStatus.CREATED
        );
        BookingUpsertRequest partiallyNullableBookingRequest = new BookingUpsertRequest(
                1L,
                2L,
                LocalDateTime.parse("2026-05-01T18:00:00"),
                null,
                BookingStatus.CREATED
        );
        OpenGameUpsertRequest validOpenGameRequest = new OpenGameUpsertRequest(
                1L,
                2L,
                40,
                70,
                12,
                OpenGameStatus.OPEN,
                List.of(3L)
        );
        OpenGameUpsertRequest invalidOpenGameRequest = new OpenGameUpsertRequest(
                1L,
                2L,
                80,
                70,
                12,
                OpenGameStatus.OPEN,
                List.of(3L)
        );
        OpenGameUpsertRequest nullableOpenGameRequest = new OpenGameUpsertRequest(
                1L,
                2L,
                null,
                null,
                12,
                OpenGameStatus.OPEN,
                List.of(3L)
        );
        OpenGameUpsertRequest partiallyNullableOpenGameRequest = new OpenGameUpsertRequest(
                1L,
                2L,
                40,
                null,
                12,
                OpenGameStatus.OPEN,
                List.of(3L)
        );
        BookingSearchRequest request = new BookingSearchRequest();
        BookingSearchRequest partiallyNullableRangeRequest = new BookingSearchRequest();

        request.setDistrict(" Central ");
        request.setPitchType(PitchType.EIGHT);
        request.setOrganizerName(" Alexey ");
        request.setStatus(BookingStatus.CONFIRMED);
        request.setStartFrom(LocalDateTime.parse("2026-05-01T18:00:00"));
        request.setStartTo(LocalDateTime.parse("2026-05-02T18:00:00"));
        request.setPage(2);
        request.setSize(10);

        assertThat(validBookingRequest.pitchId()).isEqualTo(1L);
        assertThat(validBookingRequest.organizerId()).isEqualTo(2L);
        assertThat(validBookingRequest.isTimeRangeValid()).isTrue();
        assertThat(invalidBookingRequest.isTimeRangeValid()).isFalse();
        assertThat(nullableBookingRequest.isTimeRangeValid()).isTrue();
        assertThat(partiallyNullableBookingRequest.isTimeRangeValid()).isTrue();
        assertThat(validOpenGameRequest.bookingId()).isEqualTo(1L);
        assertThat(validOpenGameRequest.organizerId()).isEqualTo(2L);
        assertThat(validOpenGameRequest.targetSkillMin()).isEqualTo(40);
        assertThat(validOpenGameRequest.targetSkillMax()).isEqualTo(70);
        assertThat(validOpenGameRequest.maxPlayers()).isEqualTo(12);
        assertThat(validOpenGameRequest.status()).isEqualTo(OpenGameStatus.OPEN);
        assertThat(validOpenGameRequest.participantIds()).containsExactly(3L);
        assertThat(validOpenGameRequest.isSkillRangeValid()).isTrue();
        assertThat(invalidOpenGameRequest.isSkillRangeValid()).isFalse();
        assertThat(nullableOpenGameRequest.isSkillRangeValid()).isTrue();
        assertThat(partiallyNullableOpenGameRequest.isSkillRangeValid()).isTrue();
        assertThat(request.getDistrict()).isEqualTo(" Central ");
        assertThat(request.getPitchType()).isEqualTo(PitchType.EIGHT);
        assertThat(request.getOrganizerName()).isEqualTo(" Alexey ");
        assertThat(request.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(request.getStartFrom()).isEqualTo(LocalDateTime.parse("2026-05-01T18:00:00"));
        assertThat(request.getStartTo()).isEqualTo(LocalDateTime.parse("2026-05-02T18:00:00"));
        assertThat(request.getPage()).isEqualTo(2);
        assertThat(request.getSize()).isEqualTo(10);
        assertThat(request.isDateRangeValid()).isTrue();

        request.setStartTo(LocalDateTime.parse("2026-04-30T18:00:00"));
        assertThat(request.isDateRangeValid()).isFalse();
        partiallyNullableRangeRequest.setStartFrom(LocalDateTime.parse("2026-05-01T18:00:00"));
        assertThat(partiallyNullableRangeRequest.isDateRangeValid()).isTrue();

        BookingSearchCriteria criteria = request.toCriteria();
        BookingSearchCriteria blankCriteria = new BookingSearchCriteria("   ", null, "   ", null, null, null);
        assertThat(criteria.district()).isEqualTo("central");
        assertThat(criteria.organizerName()).isEqualTo("alexey");
        assertThat(criteria.pitchType()).isEqualTo(PitchType.EIGHT);
        assertThat(criteria.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(blankCriteria.district()).isNull();
        assertThat(blankCriteria.organizerName()).isNull();
    }

    @Test
    void shouldCoverBookingSearchCacheKeyEquality() {
        BookingSearchCriteria criteria = new BookingSearchCriteria("Central", PitchType.EIGHT, "Alexey",
                BookingStatus.CREATED, null, null);
        BookingSearchCacheKey first = new BookingSearchCacheKey("jpql", criteria, 0, 5);
        BookingSearchCacheKey second = new BookingSearchCacheKey("jpql", criteria, 0, 5);
        BookingSearchCacheKey differentPage = new BookingSearchCacheKey("jpql", criteria, 1, 5);
        BookingSearchCacheKey differentSize = new BookingSearchCacheKey("jpql", criteria, 0, 10);
        BookingSearchCacheKey differentQueryType = new BookingSearchCacheKey("native", criteria, 0, 5);
        BookingSearchCacheKey differentCriteria = new BookingSearchCacheKey(
                "jpql",
                new BookingSearchCriteria("West", PitchType.FIVE_TURF, "Pavel", BookingStatus.CONFIRMED, null, null),
                0,
                5
        );

        assertThat(first).isEqualTo(first);
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(differentPage);
        assertThat(first).isNotEqualTo(differentSize);
        assertThat(first).isNotEqualTo(differentQueryType);
        assertThat(first).isNotEqualTo(differentCriteria);
        assertThat(first).isNotEqualTo("not-a-key");
    }
}
