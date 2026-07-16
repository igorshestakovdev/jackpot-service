package com.example.jackpot.utils;

import com.example.jackpot.dto.RewardDecision;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.enums.ContributionType;
import com.example.jackpot.enums.RewardType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.experimental.UtilityClass;

/**
 * Contains calculation helpers for jackpot contributions and reward chances.
 */
@UtilityClass
public class JackpotMathUtils {

    /**
     * Calculates the contribution amount according to the configured jackpot mode.
     */
    public static BigDecimal calculateContribution(Jackpot jackpot, BigDecimal betAmount) {

        if (jackpot.getContributionType() == ContributionType.FIXED) {

            BigDecimal rate = ValidationUtils.requireRate(
                    jackpot.getFixedContributionRate(),
                    "fixedContributionRate");

            return betAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }

        if (jackpot.getContributionType() == ContributionType.VARIABLE) {

            BigDecimal initialRate = ValidationUtils.requireRate(
                    jackpot.getVariableInitialRate(),
                    "variableInitialRate");
            BigDecimal minimumRate = ValidationUtils.requireRate(
                    jackpot.getVariableMinRate(),
                    "variableMinRate");
            BigDecimal decrementRate = ValidationUtils.requireNonNegative(
                    jackpot.getVariableDecrementRate(),
                    "variableDecrementRate");

            if (minimumRate.compareTo(initialRate) > 0) {
                throw new IllegalStateException("variableMinRate must not exceed variableInitialRate");
            }

            BigDecimal currentRate = initialRate
                    .subtract(decrementRate.multiply(poolGrowth(jackpot)))
                    .max(minimumRate);

            return betAmount.multiply(currentRate).setScale(2, RoundingMode.HALF_UP);
        }

        throw new IllegalStateException("Unsupported contribution type: " + jackpot.getContributionType());
    }

    /**
     * Evaluates whether the current random value results in a reward.
     */
    public static RewardDecision evaluateReward(Jackpot jackpot, BigDecimal randomValue) {

        validateRandomValue(randomValue);

        BigDecimal chance = calculateRewardChance(jackpot).max(BigDecimal.ZERO).min(BigDecimal.ONE);
        boolean won = randomValue.compareTo(chance) < 0;

        return new RewardDecision(won, chance, randomValue);
    }

    /**
     * Calculates the effective reward chance for the current jackpot state.
     */
    private static BigDecimal calculateRewardChance(Jackpot jackpot) {

        if (jackpot.getRewardType() == RewardType.FIXED) {
            return ValidationUtils.requireRate(jackpot.getFixedRewardChance(), "fixedRewardChance");
        }

        if (jackpot.getRewardType() == RewardType.VARIABLE) {

            BigDecimal limitPool = ValidationUtils.requireNonNegative(
                    jackpot.getVariableLimitPool(),
                    "variableLimitPool");

            if (jackpot.getCurrentPool().compareTo(limitPool) >= 0) {
                return BigDecimal.ONE;
            }

            BigDecimal initialChance = ValidationUtils.requireRate(
                    jackpot.getVariableInitialChance(),
                    "variableInitialChance");
            BigDecimal coefficient = ValidationUtils.requireNonNegative(
                    jackpot.getVariableCoefficient(),
                    "variableCoefficient");

            return initialChance.add(coefficient.multiply(poolGrowth(jackpot))).min(BigDecimal.ONE);
        }

        throw new IllegalStateException("Unsupported reward type: " + jackpot.getRewardType());
    }

    /**
     * Returns the pool growth above the initial amount and never below zero.
     */
    private static BigDecimal poolGrowth(Jackpot jackpot) {
        return jackpot.getCurrentPool().subtract(jackpot.getInitialPool()).max(BigDecimal.ZERO);
    }

    /**
     * Ensures the random value is within the supported range [0, 1).
     */
    private static void validateRandomValue(BigDecimal randomValue) {

        if (randomValue == null || randomValue.signum() < 0 || randomValue.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalStateException("Random value must be in range [0, 1)");
        }
    }
}
