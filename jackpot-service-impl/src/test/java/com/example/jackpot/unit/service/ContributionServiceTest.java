package com.example.jackpot.unit.service;

import static com.example.jackpot.utils.TestUtils.NOW;
import static com.example.jackpot.utils.TestUtils.aBet;
import static com.example.jackpot.utils.TestUtils.aContribution;
import static com.example.jackpot.utils.TestUtils.amount;
import static com.example.jackpot.utils.TestUtils.fixedJackpot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jackpot.dto.ContributionData;
import com.example.jackpot.dto.ContributionResult;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.enums.ContributionStatus;
import com.example.jackpot.repository.JackpotContributionRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.service.impl.ContributionServiceImpl;
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
class ContributionServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JackpotContributionRepository contributionRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private ContributionServiceImpl service;

    @BeforeEach
    void setUp() {

        service = new ContributionServiceImpl(
                jackpotRepository,
                contributionRepository,
                Clock.fixed(NOW, ZoneOffset.UTC),
                transactionTemplate);

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null));
    }

    @Test
    void updatesPoolAndPersistsContributionForNewBet() {

        Jackpot jackpot = fixedJackpot(amount("1000.00"), amount("1000.00"), amount("0.10"), amount("0.01"));

        var bet = aBet("bet-1", "user-1", jackpot.getId(), amount("100.00"));

        when(jackpotRepository.findByIdForUpdate(jackpot.getId())).thenReturn(Optional.of(jackpot));
        when(contributionRepository.findByBetId(bet.getBetId())).thenReturn(Optional.empty());
        when(contributionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ContributionResult result = service.contribute(bet);

        assertThat(result.getStatus()).isEqualTo(ContributionStatus.CREATED);

        ContributionData contribution = result.getContribution();

        assertThat(contribution.getContributionAmount()).isEqualByComparingTo("10.00");
        assertThat(contribution.getCurrentJackpotAmount()).isEqualByComparingTo("1010.00");
        assertThat(contribution.getCreatedAt()).isEqualTo(NOW);

        var jackpotCaptor = ArgumentCaptor.forClass(Jackpot.class);

        verify(jackpotRepository).save(jackpotCaptor.capture());

        assertThat(jackpotCaptor.getValue().getCurrentPool()).isEqualByComparingTo("1010.00");
    }

    @Test
    void returnsExistingContributionForDuplicateBetWithoutTouchingPool() {

        Jackpot jackpot = fixedJackpot(amount("1010.00"), amount("1000.00"), amount("0.10"), amount("0.01"));

        var bet = aBet("bet-1", "user-1", jackpot.getId(), amount("100.00"));

        var existing = aContribution(
                1L,
                bet.getBetId(),
                bet.getUserId(),
                bet.getJackpotId(),
                bet.getBetAmount(),
                amount("10.00"),
                jackpot.getCurrentPool(),
                NOW);

        when(jackpotRepository.findByIdForUpdate(jackpot.getId())).thenReturn(Optional.of(jackpot));
        when(contributionRepository.findByBetId(bet.getBetId())).thenReturn(Optional.of(existing));

        ContributionResult result = service.contribute(bet);

        assertThat(result.getStatus()).isEqualTo(ContributionStatus.DUPLICATE);

        assertThat(result.getContribution()).isEqualTo(
                new ContributionData(
                        existing.getBetId(),
                        existing.getUserId(),
                        existing.getJackpotId(),
                        existing.getStakeAmount(),
                        existing.getContributionAmount(),
                        existing.getCurrentJackpotAmount(),
                        existing.getCreatedAt()));

        verify(jackpotRepository, never()).save(any());
    }

    @Test
    void returnsNotFoundStatusWhenJackpotDoesNotExist() {

        var bet = aBet("bet-1", "user-1", "missing", amount("100.00"));
        when(jackpotRepository.findByIdForUpdate("missing")).thenReturn(Optional.empty());

        ContributionResult result = service.contribute(bet);

        assertThat(result.getStatus()).isEqualTo(ContributionStatus.JACKPOT_NOT_FOUND);
        assertThat(result.getContribution()).isNull();
        verify(contributionRepository, never()).findByBetId(any());
    }
}
