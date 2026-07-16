package com.example.jackpot.integration.concurrency;

import static com.example.jackpot.utils.TestUtils.aBet;
import static com.example.jackpot.utils.TestUtils.aCommand;
import static com.example.jackpot.utils.TestUtils.amount;
import static org.assertj.core.api.Assertions.assertThat;
import static com.example.jackpot.utils.ConcurrentTestSupport.submitConcurrently;

import com.example.jackpot.dto.RewardEvaluationResult;
import com.example.jackpot.integration.core.AbstractIntegrationTest;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class RewardEvaluationConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Test
    void paysExactlyOnceWhenSameBetIsEvaluatedConcurrently() throws Exception {

        contributionService.contribute(aBet("concurrent-winner", "integration-user", FIXED_JACKPOT_ID, amount("500.00")));

        var command = aCommand("concurrent-winner", "integration-user", FIXED_JACKPOT_ID);

        int taskCount = 16;
        var executor = newExecutor(taskCount);

        for (Future<RewardEvaluationResult> future : submitConcurrently(
                executor,
                taskCount,
                index -> () -> rewardService.evaluate(command))) {
            RewardEvaluationResult result = future.get(10, TimeUnit.SECONDS);

            assertThat(result.isWon()).isTrue();
            assertThat(result.getRewardAmount()).isEqualByComparingTo("1050.00");
        }

        assertThat(randomProvider.invocationCount()).isEqualTo(1);
        assertThat(rewardRepository.countByJackpotId(FIXED_JACKPOT_ID)).isEqualTo(1);
        assertThat(evaluationRepository.findByBetId(command.getBetId())).isPresent();

        assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                .isEqualByComparingTo("1000.00");
    }
}
