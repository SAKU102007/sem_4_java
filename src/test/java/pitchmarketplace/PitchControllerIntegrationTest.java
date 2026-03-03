package pitchmarketplace;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PitchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldFilterByTypePriceAndSkill() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("pitchType", "FIVE_FUTSAL")
                        .param("priceTo", "120")
                        .param("skillMin", "60")
                        .param("skillMax", "75"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pitchId", is(1)));
    }

    @Test
    void shouldSortByPrice() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pitchId", is(2)));

        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("sort", "price_desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pitchId", is(4)));
    }

    @Test
    void shouldSortByAverageSkill() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("sort", "avg_skill_desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pitchId", is(3)));

        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("sort", "avg_skill_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pitchId", is(2)));
    }

    @Test
    void shouldSortByDistanceToMeWithAuthorizedUser() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("sort", "distance_to_me_asc")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pitchId", is(2)));
    }

    @Test
    void shouldFailDistanceToMeWithoutUserHeader() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("sort", "distance_to_me_asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldApplyStrictTimeAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("desiredStartAt", "2026-03-01T19:00:00")
                        .param("desiredEndAt", "2026-03-01T21:00:00")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].pitchId", is(2)))
                .andExpect(jsonPath("$[*].pitchId", not(org.hamcrest.Matchers.hasItem(1))))
                .andExpect(jsonPath("$[*].pitchId", not(org.hamcrest.Matchers.hasItem(5))));
    }

    @Test
    void shouldApplyOptionalInventoryFilter() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("needInventory", "true")
                        .param("ballQty", "5")
                        .param("bibsQty", "5")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pitchId", is(1)));

        mockMvc.perform(get("/api/v1/pitches/search")
                        .param("needInventory", "false")
                        .param("ballQty", "5")
                        .param("bibsQty", "5")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void shouldReturnPitchByPathVariable() throws Exception {
        mockMvc.perform(get("/api/v1/pitches/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pitchId", is(1)))
                .andExpect(jsonPath("$.venueName", is("Футбольный манеж Минск Арена")));
    }
}
