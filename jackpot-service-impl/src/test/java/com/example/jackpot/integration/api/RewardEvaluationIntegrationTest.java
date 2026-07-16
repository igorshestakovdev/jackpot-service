package com.example.jackpot.integration.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.example.jackpot.utils.ApiRequestFactory.rewardEvaluationRequest;
import static com.example.jackpot.utils.TestUtils.aBet;
import static com.example.jackpot.utils.TestUtils.amount;
import static com.example.jackpot.utils.TestUtils.uuid;

import com.example.jackpot.integration.core.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class RewardEvaluationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void paysWinnerResetsPoolAndKeepsEndpointIdempotent() throws Exception {
        var betId = uuid("winning-bet");

        contributionService.contribute(aBet(betId, "integration-user", FIXED_JACKPOT_ID, amount("100.00")));

        String request = rewardEvaluationRequest(betId, "integration-user", FIXED_JACKPOT_ID);

        String firstResponse = mockMvc
                .perform(
                        post("/api/v1/jackpots/evaluate")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.rewardAmount").value(1010.00))
                .andExpect(jsonPath("$.evaluatedChance").value(0.001))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc
                .perform(
                        post("/api/v1/jackpots/evaluate")
                                .contentType("application/json")
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(secondResponse).isEqualTo(firstResponse);
        assertThat(randomProvider.invocationCount()).isEqualTo(1);

        assertThat(rewardRepository.findByBetId(betId))
                .get()
                .extracting(reward -> reward.getJackpotRewardAmount())
                .isEqualTo(amount("1010.00"));

        assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                .isEqualByComparingTo("1000.00");
    }
}
