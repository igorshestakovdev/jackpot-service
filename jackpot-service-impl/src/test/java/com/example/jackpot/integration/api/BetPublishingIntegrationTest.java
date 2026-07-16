package com.example.jackpot.integration.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.example.jackpot.utils.ApiRequestFactory.publishBetRequest;
import static com.example.jackpot.utils.TestUtils.aBet;
import static com.example.jackpot.utils.TestUtils.amount;
import static com.example.jackpot.utils.TestUtils.uuid;

import com.example.jackpot.integration.core.AbstractIntegrationTest;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class BetPublishingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void publishesBetToKafkaAndPersistsContribution() throws Exception {
        var betId = uuid("integration-bet-1");

        mockMvc
                .perform(
                        post("/api/v1/bets")
                                .contentType("application/json")
                                .content(
                                        publishBetRequest(
                                                betId,
                                                "integration-user",
                                                FIXED_JACKPOT_ID,
                                                amount("100.00")
                                        )
                                )
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.betId").value(betId.toString()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(contributionRepository.findByBetId(betId))
                        .get()
                        .satisfies(contribution -> {
                            assertThat(contribution.getContributionAmount()).isEqualByComparingTo("10.00");
                            assertThat(contribution.getCurrentJackpotAmount()).isEqualByComparingTo("1010.00");
                        }));

        assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                .isEqualByComparingTo("1010.00");
    }

    @Test
    void ignoresDuplicateKafkaDeliveryWithoutIncreasingPoolTwice() throws Exception {

        var bet = aBet(uuid("duplicate-bet"), "integration-user", FIXED_JACKPOT_ID, amount("100.00"));

        kafkaTemplate.send("jackpot-bets", bet.getJackpotId(), bet).get(5, TimeUnit.SECONDS);

        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(contributionRepository.findByBetId(bet.getBetId())).isPresent());

        kafkaTemplate.send("jackpot-bets", bet.getJackpotId(), bet).get(5, TimeUnit.SECONDS);
        kafkaTemplate.flush();

        await()
                .pollDelay(Duration.ofMillis(500))
                .during(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(contributionRepository.countByJackpotId(FIXED_JACKPOT_ID)).isEqualTo(1);
                    assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                            .isEqualByComparingTo("1010.00");
                });
    }
}
