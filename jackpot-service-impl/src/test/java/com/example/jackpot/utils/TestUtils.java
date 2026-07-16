package com.example.jackpot.utils;

import com.example.jackpot.dto.Bet;
import com.example.jackpot.dto.RewardEvaluationCommand;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.JackpotContribution;
import com.example.jackpot.entity.JackpotRewardEvaluation;
import com.example.jackpot.enums.ContributionType;
import com.example.jackpot.enums.RewardType;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Central test utilities and factories used across unit and integration tests.
 */
@UtilityClass
public final class TestUtils {

    public static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");

    private static final UUID DEFAULT_BET_ID = uuid("bet-1");
    private static final String DEFAULT_USER_ID = "user-1";
    private static final String DEFAULT_JACKPOT_ID = "fixed-jackpot";
    private static final BigDecimal DEFAULT_BET_AMOUNT = new BigDecimal("100.00");

    /** Creates a BigDecimal with readable inline syntax for tests. */
    public static BigDecimal amount(String value) {

        return new BigDecimal(value);
    }

    /** Returns a default valid bet for tests that do not care about field values. */
    public static Bet aBet() {

        return new Bet(DEFAULT_BET_ID, DEFAULT_USER_ID, DEFAULT_JACKPOT_ID, DEFAULT_BET_AMOUNT);
    }

    /** Creates a bet with explicit values for service and integration scenarios. */
    public static Bet aBet(String betId, String userId, String jackpotId, BigDecimal betAmount) {

        return new Bet(uuid(betId), userId, jackpotId, betAmount);
    }

    /** Creates a bet with an explicit UUID for tests that verify persistence keys. */
    public static Bet aBet(UUID betId, String userId, String jackpotId, BigDecimal betAmount) {

        return new Bet(betId, userId, jackpotId, betAmount);
    }

    /** Creates a stored contribution snapshot for duplicate and reward-related tests. */
    public static JackpotContribution aContribution(Long id, String betId, String userId, String jackpotId, BigDecimal stakeAmount, BigDecimal contributionAmount, BigDecimal currentJackpotAmount, Instant createdAt) {

        return new JackpotContribution(id, uuid(betId), userId, jackpotId, stakeAmount, contributionAmount, currentJackpotAmount, createdAt);
    }

    /** Creates a stored contribution snapshot with an explicit UUID. */
    public static JackpotContribution aContribution(Long id, UUID betId, String userId, String jackpotId, BigDecimal stakeAmount, BigDecimal contributionAmount, BigDecimal currentJackpotAmount, Instant createdAt) {

        return new JackpotContribution(id, betId, userId, jackpotId, stakeAmount, contributionAmount, currentJackpotAmount, createdAt);
    }

    /** Creates a fixed jackpot configuration for standard contribution and reward checks. */
    public static Jackpot fixedJackpot(BigDecimal currentPool, BigDecimal initialPool, BigDecimal contributionRate, BigDecimal rewardChance) {

        return new Jackpot("fixed-jackpot", "Fixed Jackpot", currentPool, initialPool, ContributionType.FIXED, contributionRate, null, null, null, RewardType.FIXED, rewardChance, null, null, null, 0L);
    }

    /** Creates a variable-contribution jackpot for rate progression calculations. */
    public static Jackpot variableContributionJackpot(BigDecimal currentPool, BigDecimal initialPool, BigDecimal initialRate, BigDecimal minimumRate, BigDecimal decrementRate) {

        return new Jackpot("variable-contribution-jackpot", "Variable Contribution Jackpot", currentPool, initialPool, ContributionType.VARIABLE, null, initialRate, minimumRate, decrementRate, RewardType.FIXED, new BigDecimal("0.001"), null, null, null, 0L);
    }

    /** Creates a variable-reward jackpot for chance growth calculations. */
    public static Jackpot variableRewardJackpot(BigDecimal currentPool, BigDecimal initialPool, BigDecimal initialChance, BigDecimal coefficient, BigDecimal limitPool) {

        return new Jackpot("variable-reward-jackpot", "Variable Reward Jackpot", currentPool, initialPool, ContributionType.FIXED, new BigDecimal("0.10"), null, null, null, RewardType.VARIABLE, null, initialChance, coefficient, limitPool, 0L);
    }

    /** Rebuilds a jackpot with another pool amount for math-focused assertions. */
    public static Jackpot withCurrentPool(Jackpot jackpot, BigDecimal currentPool) {

        return new Jackpot(jackpot.getId(), jackpot.getName(), currentPool, jackpot.getInitialPool(), jackpot.getContributionType(), jackpot.getFixedContributionRate(), jackpot.getVariableInitialRate(), jackpot.getVariableMinRate(), jackpot.getVariableDecrementRate(), jackpot.getRewardType(), jackpot.getFixedRewardChance(), jackpot.getVariableInitialChance(), jackpot.getVariableCoefficient(), jackpot.getVariableLimitPool(), jackpot.getVersion());
    }

    /** Creates a reward evaluation command for reward service and endpoint tests. */
    public static RewardEvaluationCommand aCommand(String betId, String userId, String jackpotId) {

        return new RewardEvaluationCommand(uuid(betId), userId, jackpotId);
    }

    /** Creates a reward evaluation command with an explicit UUID. */
    public static RewardEvaluationCommand aCommand(UUID betId, String userId, String jackpotId) {

        return new RewardEvaluationCommand(betId, userId, jackpotId);
    }

    /** Creates a stored evaluation snapshot for idempotency and replay scenarios. */
    public static JackpotRewardEvaluation anEvaluation(Long id, String betId, String userId, String jackpotId, boolean won, BigDecimal evaluatedChance, BigDecimal randomValue, BigDecimal rewardAmount, Instant createdAt) {

        return new JackpotRewardEvaluation(id, uuid(betId), userId, jackpotId, won, evaluatedChance, randomValue, rewardAmount, createdAt);
    }

    /** Creates a stored evaluation snapshot with an explicit UUID. */
    public static JackpotRewardEvaluation anEvaluation(Long id, UUID betId, String userId, String jackpotId, boolean won, BigDecimal evaluatedChance, BigDecimal randomValue, BigDecimal rewardAmount, Instant createdAt) {

        return new JackpotRewardEvaluation(id, betId, userId, jackpotId, won, evaluatedChance, randomValue, rewardAmount, createdAt);
    }

    /** Creates deterministic UUIDs from readable labels so tests stay easy to follow. */
    public static UUID uuid(String value) {

        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }
}
