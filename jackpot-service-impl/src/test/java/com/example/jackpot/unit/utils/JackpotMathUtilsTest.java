package com.example.jackpot.unit.utils;

import static com.example.jackpot.utils.TestUtils.amount;
import static com.example.jackpot.utils.TestUtils.fixedJackpot;
import static com.example.jackpot.utils.TestUtils.variableContributionJackpot;
import static com.example.jackpot.utils.TestUtils.variableRewardJackpot;
import static com.example.jackpot.utils.TestUtils.withCurrentPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.dto.RewardDecision;
import com.example.jackpot.utils.JackpotMathUtils;
import org.junit.jupiter.api.Test;

class JackpotMathUtilsTest {

    @Test
    void calculatesFixedContributionUsingHalfUpRounding() {

        var jackpot = fixedJackpot(amount("0.00"), amount("0.00"), amount("0.10"), amount("0.001"));

        assertThat(JackpotMathUtils.calculateContribution(jackpot, amount("100.00")))
                .isEqualByComparingTo("10.00");
        assertThat(JackpotMathUtils.calculateContribution(jackpot, amount("100.05")))
                .isEqualByComparingTo("10.01");
        assertThat(JackpotMathUtils.calculateContribution(jackpot, amount("100.04")))
                .isEqualByComparingTo("10.00");
    }

    @Test
    void decreasesVariableContributionRateAsPoolGrowsButNeverBelowMinimum() {

        var jackpot = variableContributionJackpot(
                amount("500.00"),
                amount("500.00"),
                amount("0.20"),
                amount("0.05"),
                amount("0.0001"));

        assertThat(JackpotMathUtils.calculateContribution(jackpot, amount("100.00")))
                .isEqualByComparingTo("20.00");

        assertThat(JackpotMathUtils.calculateContribution(
                withCurrentPool(jackpot, amount("1000.00")),
                amount("100.00"))).isEqualByComparingTo("15.00");

        assertThat(JackpotMathUtils.calculateContribution(
                withCurrentPool(jackpot, amount("3000.00")),
                amount("100.00"))).isEqualByComparingTo("5.00");
    }

    @Test
    void rejectsVariableContributionConfigurationWhenMinimumRateExceedsInitialRate() {

        var jackpot = variableContributionJackpot(
                amount("500.00"),
                amount("500.00"),
                amount("0.10"),
                amount("0.15"),
                amount("0.0001"));

        assertThatThrownBy(() -> JackpotMathUtils.calculateContribution(jackpot, amount("100.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("variableMinRate must not exceed variableInitialRate");
    }

    @Test
    void evaluatesFixedRewardUsingStrictLessThanComparison() {

        var jackpot = fixedJackpot(amount("0.00"), amount("0.00"), amount("0.10"), amount("0.001"));

        RewardDecision win = JackpotMathUtils.evaluateReward(jackpot, amount("0.0009"));
        RewardDecision equalBoundaryLoss = JackpotMathUtils.evaluateReward(jackpot, amount("0.0010"));
        RewardDecision greaterThanBoundaryLoss = JackpotMathUtils.evaluateReward(jackpot, amount("0.05"));

        assertThat(win.isWon()).isTrue();
        assertThat(win.getChance()).isEqualByComparingTo("0.001");
        assertThat(equalBoundaryLoss.isWon()).isFalse();
        assertThat(greaterThanBoundaryLoss.isWon()).isFalse();
    }

    @Test
    void increasesVariableRewardChanceUntilItCapsAtOne() {

        var jackpot = variableRewardJackpot(
                amount("500.00"),
                amount("500.00"),
                amount("0.001"),
                amount("0.0005"),
                amount("2500.00"));

        assertThat(JackpotMathUtils.evaluateReward(jackpot, amount("0.0005")).getChance())
                .isEqualByComparingTo("0.001");

        assertThat(JackpotMathUtils.evaluateReward(
                withCurrentPool(jackpot, amount("1500.00")),
                amount("0.50")).getChance()).isEqualByComparingTo("0.501");

        assertThat(JackpotMathUtils.evaluateReward(
                withCurrentPool(jackpot, amount("2500.00")),
                amount("0.9999")).getChance()).isEqualByComparingTo("1.0");

        assertThat(JackpotMathUtils.evaluateReward(
                withCurrentPool(jackpot, amount("3000.00")),
                amount("0.9999")).getChance()).isEqualByComparingTo("1.0");
    }

    @Test
    void rejectsRandomValuesOutsideSupportedRange() {

        var jackpot = fixedJackpot(amount("0.00"), amount("0.00"), amount("0.10"), amount("0.001"));

        assertThatThrownBy(() -> JackpotMathUtils.evaluateReward(jackpot, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> JackpotMathUtils.evaluateReward(jackpot, amount("-0.01")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> JackpotMathUtils.evaluateReward(jackpot, amount("1.0")))
                .isInstanceOf(IllegalStateException.class);
    }
}
