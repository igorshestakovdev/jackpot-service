package com.example.jackpot.unit.service;

import static com.example.jackpot.utils.TestUtils.NOW;
import static com.example.jackpot.utils.TestUtils.aCommand;
import static com.example.jackpot.utils.TestUtils.aContribution;
import static com.example.jackpot.utils.TestUtils.amount;
import static com.example.jackpot.utils.TestUtils.anEvaluation;
import static com.example.jackpot.utils.TestUtils.fixedJackpot;
import static com.example.jackpot.utils.TestUtils.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.JackpotReward;
import com.example.jackpot.exception.ContributionMismatchException;
import com.example.jackpot.exception.ContributionNotFoundException;
import com.example.jackpot.exception.JackpotNotFoundException;
import com.example.jackpot.repository.JackpotContributionRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.JackpotRewardEvaluationRepository;
import com.example.jackpot.repository.JackpotRewardRepository;
import com.example.jackpot.service.RandomProvider;
import com.example.jackpot.service.impl.RewardServiceImpl;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @Mock
    private JackpotRewardRepository rewardRepository;

    @Mock
    private JackpotRewardEvaluationRepository evaluationRepository;

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private TransactionTemplate transactionTemplate;

    private RewardServiceImpl service;

    @BeforeEach
    void setUp() {

        service = new RewardServiceImpl(
                jackpotRepository,
                contributionRepository,
                rewardRepository,
                evaluationRepository,
                randomProvider,
                Clock.fixed(NOW, ZoneOffset.UTC),
                transactionTemplate);

        lenient().when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null));
    }

    @Test
    void paysCurrentPoolAndResetsJackpotOnWin() {

        TestScenario scenario = processedContributionScenario();

        when(evaluationRepository.findByBetId(scenario.command().getBetId())).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(randomProvider.nextValue()).thenReturn(amount("0.0005"));

        var result = service.evaluate(scenario.command());

        assertThat(result.isWon()).isTrue();
        assertThat(result.getRewardAmount()).isEqualByComparingTo("1010.00");

        var jackpotCaptor = ArgumentCaptor.forClass(Jackpot.class);

        verify(jackpotRepository).save(jackpotCaptor.capture());

        assertThat(jackpotCaptor.getValue().getCurrentPool()).isEqualByComparingTo("1000.00");

        var rewardCaptor = ArgumentCaptor.forClass(JackpotReward.class);

        verify(rewardRepository).save(rewardCaptor.capture());

        assertThat(rewardCaptor.getValue().getJackpotRewardAmount()).isEqualByComparingTo("1010.00");
    }

    @Test
    void persistsLossEvaluationWithoutChangingPool() {

        TestScenario scenario = processedContributionScenario();

        when(evaluationRepository.findByBetId(scenario.command().getBetId())).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(randomProvider.nextValue()).thenReturn(amount("0.50"));

        var result = service.evaluate(scenario.command());

        assertThat(result.isWon()).isFalse();
        assertThat(result.getRewardAmount()).isNull();

        verify(jackpotRepository, never()).save(any());
        verifyNoInteractions(rewardRepository);
    }

    @Test
    void returnsStoredEvaluationWithoutDrawingRandomValueAgain() {

        TestScenario scenario = processedContributionScenario();

        var existing = anEvaluation(
                1L,
                scenario.command().getBetId(),
                scenario.command().getUserId(),
                scenario.command().getJackpotId(),
                false,
                amount("0.10"),
                amount("0.50"),
                null,
                NOW);
        when(evaluationRepository.findByBetId(scenario.command().getBetId())).thenReturn(Optional.of(existing));

        var result = service.evaluate(scenario.command());

        assertThat(result.isWon()).isFalse();

        verifyNoInteractions(randomProvider, rewardRepository);
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    void failsWhenProcessedContributionIsMissing() {
        var command = aCommand("missing", "user-1", "jackpot-1");
        when(contributionRepository.findByBetId(command.getBetId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluate(command))
                .isInstanceOf(ContributionNotFoundException.class)
                .hasMessageContaining(uuid("missing").toString());
    }

    @Test
    void failsWhenJackpotIsMissing() {

        var command = aCommand("bet-1", "user-1", "missing");
        var contribution = aContribution(
                1L,
                command.getBetId(),
                command.getUserId(),
                command.getJackpotId(),
                amount("100.00"),
                amount("10.00"),
                amount("1010.00"),
                NOW.minusSeconds(1));

        when(contributionRepository.findByBetId(command.getBetId())).thenReturn(Optional.of(contribution));
        when(jackpotRepository.findByIdForUpdate("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluate(command))
                .isInstanceOf(JackpotNotFoundException.class)
                .hasMessageContaining("missing");

        verifyNoInteractions(randomProvider, rewardRepository);
        verify(evaluationRepository).findByBetId(command.getBetId());
        verifyNoMoreInteractions(evaluationRepository);
    }

    @Test
    void failsWhenContributionBelongsToAnotherUser() {

        TestScenario scenario = processedContributionScenario();
        var mismatched = aCommand(scenario.command().getBetId(), "another-user", scenario.command().getJackpotId());

        assertThatThrownBy(() -> service.evaluate(mismatched))
                .isInstanceOf(ContributionMismatchException.class);
        verifyNoInteractions(randomProvider);
    }

    private TestScenario processedContributionScenario() {

        Jackpot jackpot = fixedJackpot(amount("1010.00"), amount("1000.00"), amount("0.10"), amount("0.10"));

        var command = aCommand("bet-1", "user-1", jackpot.getId());

        var contribution = aContribution(
                1L,
                command.getBetId(),
                command.getUserId(),
                command.getJackpotId(),
                amount("100.00"),
                amount("10.00"),
                jackpot.getCurrentPool(),
                NOW.minusSeconds(1));

        lenient().when(jackpotRepository.findByIdForUpdate(jackpot.getId())).thenReturn(Optional.of(jackpot));
        when(contributionRepository.findByBetId(command.getBetId())).thenReturn(Optional.of(contribution));

        return new TestScenario(jackpot, command);
    }

    private record TestScenario(Jackpot jackpot, com.example.jackpot.dto.RewardEvaluationCommand command) {}
}
