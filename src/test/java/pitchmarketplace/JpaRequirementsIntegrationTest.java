package pitchmarketplace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
}
