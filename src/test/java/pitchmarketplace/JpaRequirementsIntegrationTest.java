package pitchmarketplace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class JpaRequirementsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPerformPitchCrud() throws Exception {
        String createRequest = """
                {
                  "name": "Test Pitch",
                  "type": "FIVE_TURF",
                  "district": "Ленинский",
                  "metro": "Пролетарская",
                  "pricePerHour": 99.50
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/pitches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Pitch"))
                .andReturn();

        Long createdId = Long.valueOf(JsonPath.read(createResult.getResponse().getContentAsString(), "$.id").toString());

        mockMvc.perform(get("/api/v1/pitches/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.district").value("Ленинский"));

        String updateRequest = """
                {
                  "name": "Updated Pitch",
                  "type": "EIGHT",
                  "district": "Советский",
                  "metro": "Московская",
                  "pricePerHour": 130.00
                }
                """;

        mockMvc.perform(put("/api/v1/pitches/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pitch"))
                .andExpect(jsonPath("$.type").value("EIGHT"));

        mockMvc.perform(delete("/api/v1/pitches/{id}", createdId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pitches/{id}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldShowNPlusOneAndSolvedCase() throws Exception {
        MvcResult badResult = mockMvc.perform(get("/api/v1/demos/n-plus-one/bad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("bad_n_plus_one"))
                .andReturn();

        MvcResult solvedResult = mockMvc.perform(get("/api/v1/demos/n-plus-one/solved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("solved_with_entity_graph"))
                .andReturn();

        long badStatements = Long.parseLong(
                JsonPath.read(badResult.getResponse().getContentAsString(), "$.executedStatements").toString()
        );
        long solvedStatements = Long.parseLong(
                JsonPath.read(solvedResult.getResponse().getContentAsString(), "$.executedStatements").toString()
        );

        assertThat(badStatements).isGreaterThan(solvedStatements);
    }

    @Test
    void shouldPersistPartialDataWithoutTransaction() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/demos/transactions/without-transaction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("without_transaction"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        long beforeUsers = Long.parseLong(JsonPath.read(json, "$.before.users").toString());
        long afterUsers = Long.parseLong(JsonPath.read(json, "$.after.users").toString());

        long beforePitches = Long.parseLong(JsonPath.read(json, "$.before.pitches").toString());
        long afterPitches = Long.parseLong(JsonPath.read(json, "$.after.pitches").toString());

        long beforeBookings = Long.parseLong(JsonPath.read(json, "$.before.bookings").toString());
        long afterBookings = Long.parseLong(JsonPath.read(json, "$.after.bookings").toString());

        long beforeOffers = Long.parseLong(JsonPath.read(json, "$.before.equipmentOffers").toString());
        long afterOffers = Long.parseLong(JsonPath.read(json, "$.after.equipmentOffers").toString());

        assertThat(afterUsers).isGreaterThan(beforeUsers);
        assertThat(afterPitches).isGreaterThan(beforePitches);
        assertThat(afterBookings).isGreaterThan(beforeBookings);
        assertThat(afterOffers).isGreaterThan(beforeOffers);
    }

    @Test
    void shouldRollbackAllDataWithTransactional() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/demos/transactions/with-transaction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("with_transaction"))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        long beforeUsers = Long.parseLong(JsonPath.read(json, "$.before.users").toString());
        long afterUsers = Long.parseLong(JsonPath.read(json, "$.after.users").toString());

        long beforePitches = Long.parseLong(JsonPath.read(json, "$.before.pitches").toString());
        long afterPitches = Long.parseLong(JsonPath.read(json, "$.after.pitches").toString());

        long beforeBookings = Long.parseLong(JsonPath.read(json, "$.before.bookings").toString());
        long afterBookings = Long.parseLong(JsonPath.read(json, "$.after.bookings").toString());

        long beforeOpenGames = Long.parseLong(JsonPath.read(json, "$.before.openGames").toString());
        long afterOpenGames = Long.parseLong(JsonPath.read(json, "$.after.openGames").toString());

        long beforeOffers = Long.parseLong(JsonPath.read(json, "$.before.equipmentOffers").toString());
        long afterOffers = Long.parseLong(JsonPath.read(json, "$.after.equipmentOffers").toString());

        assertThat(afterUsers).isEqualTo(beforeUsers);
        assertThat(afterPitches).isEqualTo(beforePitches);
        assertThat(afterBookings).isEqualTo(beforeBookings);
        assertThat(afterOpenGames).isEqualTo(beforeOpenGames);
        assertThat(afterOffers).isEqualTo(beforeOffers);
    }

    @Test
    void shouldSearchBookingsViaJpqlAndNativeWithPaging() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String district = "CacheDistrict" + suffix;
        String organizerName = "Cache Search User " + suffix;

        Long organizerId = createUser(organizerName, 78);
        Long pitchId = createPitch("Cache Search Pitch " + suffix, "EIGHT", district);

        createBooking(
                pitchId,
                organizerId,
                "2026-04-10T18:00:00",
                "2026-04-10T20:00:00",
                "CONFIRMED"
        );
        createBooking(
                pitchId,
                organizerId,
                "2026-04-11T18:00:00",
                "2026-04-11T20:00:00",
                "CONFIRMED"
        );

        mockMvc.perform(get("/api/v1/bookings/search/jpql")
                        .param("district", district)
                        .param("pitchType", "EIGHT")
                        .param("organizerName", organizerName)
                        .param("status", "CONFIRMED")
                        .param("startFrom", "2026-04-10T00:00:00")
                        .param("startTo", "2026-04-12T00:00:00")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryType").value("jpql"))
                .andExpect(jsonPath("$.cacheHit").value(false))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/v1/bookings/search/native")
                        .param("district", district)
                        .param("pitchType", "EIGHT")
                        .param("organizerName", organizerName)
                        .param("status", "CONFIRMED")
                        .param("startFrom", "2026-04-10T00:00:00")
                        .param("startTo", "2026-04-12T00:00:00")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryType").value("native"))
                .andExpect(jsonPath("$.cacheHit").value(false))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldCreateBookingsInBulk() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        Long organizerId = createUser("Bulk User " + suffix, 80);
        Long pitchId = createPitch("Bulk Pitch " + suffix, "EIGHT", "Bulk District " + suffix);

        String bulkRequest = """
                [
                  {
                    "pitchId": %d,
                    "organizerId": %d,
                    "startAt": "2026-06-01T18:00:00",
                    "endAt": "2026-06-01T20:00:00",
                    "status": "CREATED"
                  },
                  {
                    "pitchId": %d,
                    "organizerId": %d,
                    "startAt": "2026-06-02T18:00:00",
                    "endAt": "2026-06-02T20:00:00",
                    "status": "CONFIRMED"
                  }
                ]
                """.formatted(pitchId, organizerId, pitchId, organizerId);

        mockMvc.perform(post("/api/v1/bookings/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pitchId").value(pitchId))
                .andExpect(jsonPath("$[1].organizerId").value(organizerId));
    }

    @Test
    void shouldShowBulkTransactionDifference() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        Long organizerId = createUser("Bulk Tx User " + suffix, 81);
        Long pitchId = createPitch("Bulk Tx Pitch " + suffix, "FIVE_TURF", "Bulk Tx District " + suffix);

        String bulkDemoRequest = """
                [
                  {
                    "pitchId": %d,
                    "organizerId": %d,
                    "startAt": "2026-06-10T18:00:00",
                    "endAt": "2026-06-10T20:00:00",
                    "status": "CREATED"
                  },
                  {
                    "pitchId": %d,
                    "organizerId": 999999,
                    "startAt": "2026-06-11T18:00:00",
                    "endAt": "2026-06-11T20:00:00",
                    "status": "CONFIRMED"
                  }
                ]
                """.formatted(pitchId, organizerId, pitchId);

        MvcResult withoutTransaction = mockMvc.perform(post("/api/v1/demos/transactions/bulk-bookings/without-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkDemoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("bulk_without_transaction"))
                .andReturn();

        String withoutTransactionJson = withoutTransaction.getResponse().getContentAsString();
        long beforeWithout = Long.parseLong(JsonPath.read(withoutTransactionJson, "$.before.bookings").toString());
        long afterWithout = Long.parseLong(JsonPath.read(withoutTransactionJson, "$.after.bookings").toString());
        assertThat(afterWithout).isEqualTo(beforeWithout + 1);

        MvcResult withTransaction = mockMvc.perform(post("/api/v1/demos/transactions/bulk-bookings/with-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkDemoRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("bulk_with_transaction"))
                .andReturn();

        String withTransactionJson = withTransaction.getResponse().getContentAsString();
        long beforeWith = Long.parseLong(JsonPath.read(withTransactionJson, "$.before.bookings").toString());
        long afterWith = Long.parseLong(JsonPath.read(withTransactionJson, "$.after.bookings").toString());
        assertThat(afterWith).isEqualTo(beforeWith);
    }

    @Test
    void shouldStartAsyncPitchLoadReportAndEventuallyComplete() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        Long organizerId = createUser("Async User " + suffix, 82);
        Long pitchId = createPitch("Async Pitch " + suffix, "EIGHT", "Async District " + suffix);

        createBooking(
                pitchId,
                organizerId,
                "2026-06-15T18:00:00",
                "2026-06-15T20:00:00",
                "CONFIRMED"
        );
        createBooking(
                pitchId,
                organizerId,
                "2026-06-16T18:00:00",
                "2026-06-16T20:00:00",
                "CANCELLED"
        );

        String request = """
                {
                  "pitchId": %d
                }
                """.formatted(pitchId);

        MvcResult accepted = mockMvc.perform(post("/api/v1/concurrency/pitch-load-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isAccepted())
                .andReturn();

        long taskId = Long.parseLong(JsonPath.read(accepted.getResponse().getContentAsString(), "$.taskId").toString());
        assertThat(accepted.getResponse().getHeader("Location"))
                .isEqualTo("/api/v1/concurrency/pitch-load-reports/" + taskId);
        String statusJson = awaitAsyncTaskCompletion(taskId);

        assertThat(JsonPath.read(statusJson, "$.status").toString()).isEqualTo("COMPLETED");
        assertThat(Long.parseLong(JsonPath.read(statusJson, "$.result.pitchId").toString())).isEqualTo(pitchId);
        assertThat(Long.parseLong(JsonPath.read(statusJson, "$.result.totalBookings").toString())).isEqualTo(2L);
        assertThat(Long.parseLong(JsonPath.read(statusJson, "$.result.confirmedBookings").toString())).isEqualTo(1L);
        assertThat(Long.parseLong(JsonPath.read(statusJson, "$.result.cancelledBookings").toString())).isEqualTo(1L);

        mockMvc.perform(get("/api/v1/concurrency/pitch-load-reports/counters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submittedTasks").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.completedTasks").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void shouldDemonstrateRaceCondition() throws Exception {
        String request = """
                {
                  "threads": 64,
                  "incrementsPerThread": 2000
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/concurrency/race-condition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threads").value(64))
                .andExpect(jsonPath("$.incrementsPerThread").value(2000))
                .andExpect(jsonPath("$.expected").value(128000))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        long expected = Long.parseLong(JsonPath.read(json, "$.expected").toString());
        long unsafeCounter = Long.parseLong(JsonPath.read(json, "$.unsafeCounter").toString());
        long synchronizedCounter = Long.parseLong(JsonPath.read(json, "$.synchronizedCounter").toString());
        long atomicCounter = Long.parseLong(JsonPath.read(json, "$.atomicCounter").toString());

        assertThat(unsafeCounter).isLessThan(expected);
        assertThat(synchronizedCounter).isEqualTo(expected);
        assertThat(atomicCounter).isEqualTo(expected);
    }

    @Test
    void shouldReturnUnifiedValidationErrorForInvalidPitchRequest() throws Exception {
        String invalidRequest = """
                {
                  "name": "",
                  "type": "EIGHT",
                  "district": "",
                  "metro": "",
                  "pricePerHour": 0
                }
                """;

        mockMvc.perform(post("/api/v1/pitches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/pitches"))
                .andExpect(jsonPath("$.details.length()").value(4));
    }

    @Test
    void shouldExposeOpenApiSpecAndSwaggerUi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Football Pitch Marketplace API"))
                .andExpect(jsonPath("$.paths['/api/v1/bookings/search/jpql']").exists());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("swagger-ui-bundle.js")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("swagger-initializer.js")));
    }

    @Test
    void shouldReuseCacheAndInvalidateAfterPitchUpdate() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String district = "CacheInvalidateDistrict" + suffix;
        String updatedDistrict = "ChangedDistrict" + suffix;
        String organizerName = "Cache Invalidate User " + suffix;
        String pitchName = "Cache Invalidate Pitch " + suffix;

        Long organizerId = createUser(organizerName, 74);
        Long pitchId = createPitch(pitchName, "FIVE_TURF", district);
        createBooking(
                pitchId,
                organizerId,
                "2026-05-10T18:00:00",
                "2026-05-10T20:00:00",
                "CREATED"
        );

        mockMvc.perform(get("/api/v1/bookings/search/jpql")
                        .param("district", district)
                        .param("organizerName", organizerName)
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheHit").value(false))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/v1/bookings/search/jpql")
                        .param("district", district)
                        .param("organizerName", organizerName)
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheHit").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1));

        String pitchUpdateRequest = """
                {
                  "name": "%s",
                  "type": "FIVE_TURF",
                  "district": "%s",
                  "metro": "CacheMetro",
                  "pricePerHour": 120.00
                }
                """.formatted(pitchName, updatedDistrict);

        mockMvc.perform(put("/api/v1/pitches/{id}", pitchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pitchUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.district").value(updatedDistrict));

        mockMvc.perform(get("/api/v1/bookings/search/jpql")
                        .param("district", district)
                        .param("organizerName", organizerName)
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheHit").value(false))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private Long createUser(String name, int rating) throws Exception {
        String request = """
                {
                  "name": "%s",
                  "rating": %d,
                  "role": "PLAYER"
                }
                """.formatted(name, rating);

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }

    private Long createPitch(String name, String type, String district) throws Exception {
        String request = """
                {
                  "name": "%s",
                  "type": "%s",
                  "district": "%s",
                  "metro": "CacheMetro",
                  "pricePerHour": 120.00
                }
                """.formatted(name, type, district);

        MvcResult result = mockMvc.perform(post("/api/v1/pitches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }

    private Long createBooking(
            Long pitchId,
            Long organizerId,
            String startAt,
            String endAt,
            String status
    ) throws Exception {
        String request = """
                {
                  "pitchId": %d,
                  "organizerId": %d,
                  "startAt": "%s",
                  "endAt": "%s",
                  "status": "%s"
                }
                """.formatted(pitchId, organizerId, startAt, endAt, status);

        MvcResult result = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }

    private String awaitAsyncTaskCompletion(long taskId) throws Exception {
        for (int attempt = 0; attempt < 50; attempt++) {
            MvcResult result = mockMvc.perform(get("/api/v1/concurrency/pitch-load-reports/{taskId}", taskId))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            String status = JsonPath.read(json, "$.status").toString();
            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                return json;
            }
            Thread.sleep(100L);
        }
        throw new IllegalStateException("Async task did not finish in time");
    }
}
