package com.example.jackpot.integration.concurrency;

import static com.example.jackpot.utils.TestUtils.aBet;
import static com.example.jackpot.utils.TestUtils.amount;
import static com.example.jackpot.utils.TestUtils.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static com.example.jackpot.utils.ConcurrentTestSupport.submitConcurrently;

import com.example.jackpot.dto.ContributionResult;
import com.example.jackpot.enums.ContributionStatus;
import com.example.jackpot.integration.core.AbstractIntegrationTest;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ContributionConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Test
    void doesNotLosePoolUpdatesUnderConcurrentContributions() throws Exception {

        int taskCount = 12;

        var executor = newExecutor(taskCount);

        for (Future<Void> future : submitConcurrently(
                executor,
                taskCount,
                index -> (java.util.concurrent.Callable<Void>) () -> {
                    String betId = "concurrent-contribution-" + index;

                    contributionService.contribute(
                            aBet(betId, "user-" + betId, FIXED_JACKPOT_ID, amount("100.00")));

                    return null;
                })) {
            future.get(10, TimeUnit.SECONDS);
        }

        assertThat(contributionRepository.countByJackpotId(FIXED_JACKPOT_ID)).isEqualTo(taskCount);

        assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                .isEqualByComparingTo("1120.00");
    }

    @Test
    void returnsDuplicateInsteadOfUniqueConstraintFailureForSameBetAcrossDifferentJackpots() throws Exception {

        var sharedBetId = uuid("shared-bet");

        var executor = newExecutor(2);

        List<Future<ContributionResult>> futures = submitConcurrently(
                executor,
                2,
                index -> () -> contributionService.contribute(
                        aBet(
                                sharedBetId,
                                "shared-user",
                                index == 0 ? FIXED_JACKPOT_ID : VARIABLE_JACKPOT_ID,
                                amount("100.00"))));

        ContributionResult firstResult = futures.get(0).get(10, TimeUnit.SECONDS);
        ContributionResult secondResult = futures.get(1).get(10, TimeUnit.SECONDS);

        assertThat(List.of(firstResult.getStatus(), secondResult.getStatus()))
                .containsExactlyInAnyOrder(ContributionStatus.CREATED, ContributionStatus.DUPLICATE);

        ContributionResult createdResult = firstResult.getStatus() == ContributionStatus.CREATED ? firstResult : secondResult;
        ContributionResult duplicateResult = firstResult.getStatus() == ContributionStatus.DUPLICATE ? firstResult : secondResult;

        assertThat(duplicateResult.getContribution()).isEqualTo(createdResult.getContribution());
        assertThat(contributionRepository.findByBetId(sharedBetId)).isPresent();

        assertThat(contributionRepository.countByJackpotId(FIXED_JACKPOT_ID)
                + contributionRepository.countByJackpotId(VARIABLE_JACKPOT_ID))
                .isEqualTo(1);

        if (FIXED_JACKPOT_ID.equals(createdResult.getContribution().getJackpotId())) {
            assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                    .isEqualByComparingTo("1010.00");
            assertThat(jackpotRepository.findById(VARIABLE_JACKPOT_ID).orElseThrow().getCurrentPool())
                    .isEqualByComparingTo("500.00");
        } else {
            assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID).orElseThrow().getCurrentPool())
                    .isEqualByComparingTo("1000.00");
            assertThat(jackpotRepository.findById(VARIABLE_JACKPOT_ID).orElseThrow().getCurrentPool())
                    .isEqualByComparingTo("520.00");
        }
    }
}
