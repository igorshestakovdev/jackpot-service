package com.example.jackpot.integration.persistence;

import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.JackpotContribution;
import com.example.jackpot.entity.JackpotReward;
import com.example.jackpot.entity.JackpotRewardEvaluation;
import com.example.jackpot.integration.core.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static com.example.jackpot.utils.TestUtils.uuid;

class RepositoryIntegrationTest extends AbstractIntegrationTest {

    @Test
    void findsSeededJackpotForUpdate() {

        assertThat(jackpotRepository.findByIdForUpdate(FIXED_JACKPOT_ID))
                .isPresent()
                .get()
                .extracting(
                        Jackpot::getId,
                        Jackpot::getCurrentPool,
                        Jackpot::getInitialPool,
                        Jackpot::getVersion)
                .containsExactly(
                        FIXED_JACKPOT_ID,
                        new BigDecimal("1000.00"),
                        new BigDecimal("1000.00"),
                        0L);
    }

    @Test
    void savesAndFindsContributionsByBetIdAndJackpotId() {
        UUID fixedBetId = uuid("contribution-bet-1");
        UUID variableBetId = uuid("contribution-bet-2");

        JackpotContribution savedFixed = contributionRepository.save(
                new JackpotContribution(
                        null,
                        fixedBetId,
                        "user-1",
                        FIXED_JACKPOT_ID,
                        new BigDecimal("100.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("1010.00"),
                        Instant.parse("2026-07-16T00:00:00Z")
                )
        );

        contributionRepository.save(
                new JackpotContribution(
                        null,
                        variableBetId,
                        "user-2",
                        VARIABLE_JACKPOT_ID,
                        new BigDecimal("50.00"),
                        new BigDecimal("5.00"),
                        new BigDecimal("505.00"),
                        Instant.parse("2026-07-16T00:01:00Z")
                )
        );

        assertThat(savedFixed.getId()).isNotNull();

        assertThat(contributionRepository.countByJackpotId(FIXED_JACKPOT_ID)).isEqualTo(1);

        assertThat(contributionRepository.countByJackpotId(VARIABLE_JACKPOT_ID)).isEqualTo(1);

        assertThat(contributionRepository.findByBetId(fixedBetId))
                .isPresent()
                .get()
                .extracting(
                        JackpotContribution::getUserId,
                        JackpotContribution::getContributionAmount,
                        JackpotContribution::getCurrentJackpotAmount)
                .containsExactly("user-1", new BigDecimal("10.00"), new BigDecimal("1010.00"));
    }

    @Test
    void savesAndFindsRewardsByBetIdAndJackpotId() {
        UUID fixedBetId = uuid("reward-bet-1");
        UUID variableBetId = uuid("reward-bet-2");

        JackpotReward savedReward = rewardRepository.save(

                new JackpotReward(
                        null,
                        fixedBetId,
                        "winner-1",
                        FIXED_JACKPOT_ID,
                        new BigDecimal("250.00"),
                        Instant.parse("2026-07-16T00:02:00Z")
                )
        );

        rewardRepository.save(

                new JackpotReward(
                        null,
                        variableBetId,
                        "winner-2",
                        VARIABLE_JACKPOT_ID,
                        new BigDecimal("125.00"),
                        Instant.parse("2026-07-16T00:03:00Z")
                )
        );

        assertThat(savedReward.getId()).isNotNull();

        assertThat(rewardRepository.countByJackpotId(FIXED_JACKPOT_ID)).isEqualTo(1);

        assertThat(rewardRepository.countByJackpotId(VARIABLE_JACKPOT_ID)).isEqualTo(1);

        assertThat(rewardRepository.findByBetId(fixedBetId))
                .isPresent()
                .get()
                .extracting(JackpotReward::getUserId, JackpotReward::getJackpotRewardAmount)
                .containsExactly("winner-1", new BigDecimal("250.00"));
    }

    @Test
    void savesAndFindsRewardEvaluationsByBetId() {
        UUID betId = uuid("evaluation-bet-1");

        contributionRepository.save(

                new JackpotContribution(
                        null,
                        betId,
                        "user-3",
                        FIXED_JACKPOT_ID,
                        new BigDecimal("20.00"),
                        new BigDecimal("2.00"),
                        new BigDecimal("1002.00"),
                        Instant.parse("2026-07-16T00:04:00Z")
                )
        );

        JackpotRewardEvaluation savedEvaluation = evaluationRepository.save(

                new JackpotRewardEvaluation(
                        null,
                        betId,
                        "user-3",
                        FIXED_JACKPOT_ID,
                        true,
                        new BigDecimal("0.1000000000"),
                        new BigDecimal("0.0500000000"),
                        new BigDecimal("1002.00"),
                        Instant.parse("2026-07-16T00:05:00Z")
                )
        );

        assertThat(savedEvaluation.getId()).isNotNull();

        assertThat(evaluationRepository.findByBetId(betId))
                .isPresent()
                .get()
                .extracting(
                        JackpotRewardEvaluation::isWon,
                        JackpotRewardEvaluation::getEvaluatedChance,
                        JackpotRewardEvaluation::getRewardAmount)
                .containsExactly(true, new BigDecimal("0.1000000000"), new BigDecimal("1002.00"));
    }
}
