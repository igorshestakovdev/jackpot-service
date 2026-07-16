package com.example.jackpot.service.impl;

import com.example.jackpot.dto.Bet;
import com.example.jackpot.dto.ContributionData;
import com.example.jackpot.dto.ContributionResult;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.JackpotContribution;
import com.example.jackpot.enums.ContributionStatus;
import com.example.jackpot.repository.JackpotContributionRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.service.ContributionService;
import com.example.jackpot.utils.JackpotMathUtils;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {

    private final ConcurrentHashMap<UUID, BetLock> betLocks = new ConcurrentHashMap<>();

    private final JackpotRepository jackpotRepository;

    private final JackpotContributionRepository contributionRepository;

    private final Clock clock;

    private final TransactionTemplate transactionTemplate;

    @Override
    public ContributionResult contribute(Bet bet) {

        BetLock betLock = acquireBetLock(bet.getBetId());

        try {
            return transactionTemplate.execute(status -> {
                Jackpot jackpot = jackpotRepository.findByIdForUpdate(bet.getJackpotId()).orElse(null);

                if (jackpot == null) {
                    log.warn(
                            "Contribution skipped because jackpot was not found: betId={}, jackpotId={}",
                            bet.getBetId(),
                            bet.getJackpotId());
                    return new ContributionResult(ContributionStatus.JACKPOT_NOT_FOUND, null);
                }

                JackpotContribution existingContribution = contributionRepository.findByBetId(bet.getBetId()).orElse(null);

                if (existingContribution != null) {
                    log.info(
                            "Contribution already exists for bet: betId={}, jackpotId={}, contributionAmount={}, currentJackpotAmount={}",
                            existingContribution.getBetId(),
                            existingContribution.getJackpotId(),
                            existingContribution.getContributionAmount(),
                            existingContribution.getCurrentJackpotAmount());
                    return new ContributionResult(ContributionStatus.DUPLICATE, toContributionData(existingContribution));
                }

                BigDecimal previousPool = jackpot.getCurrentPool();
                BigDecimal contributionAmount = JackpotMathUtils.calculateContribution(jackpot, bet.getBetAmount());
                BigDecimal currentPool = previousPool.add(contributionAmount);

                jackpotRepository.save(withCurrentPool(jackpot, currentPool));

                log.info(
                        "Jackpot pool updated after contribution: jackpotId={}, betId={}, previousPool={}, contributionAmount={}, currentPool={}",
                        jackpot.getId(),
                        bet.getBetId(),
                        previousPool,
                        contributionAmount,
                        currentPool);

                JackpotContribution contribution = new JackpotContribution(
                        null,
                        bet.getBetId(),
                        bet.getUserId(),
                        bet.getJackpotId(),
                        bet.getBetAmount(),
                        contributionAmount,
                        currentPool,
                        clock.instant().truncatedTo(ChronoUnit.MICROS));

                return new ContributionResult(
                        ContributionStatus.CREATED,
                        toContributionData(contributionRepository.save(contribution))
                );
            });
        } finally {
            releaseBetLock(bet.getBetId(), betLock);
        }
    }

    private BetLock acquireBetLock(UUID betId) {

        BetLock betLock = betLocks.compute(betId, (ignored, existingLock) -> {
            if (existingLock == null) {
                return new BetLock();
            }

            existingLock.retain();
            return existingLock;
        });

        betLock.lock();
        return betLock;
    }

    private void releaseBetLock(UUID betId, BetLock betLock) {

        try {
            betLock.unlock();
        } finally {
            if (betLock.release() == 0) {
                betLocks.remove(betId, betLock);
            }
        }
    }

    private static Jackpot withCurrentPool(Jackpot jackpot, BigDecimal currentPool) {

        return new Jackpot(
                jackpot.getId(),
                jackpot.getName(),
                currentPool,
                jackpot.getInitialPool(),
                jackpot.getContributionType(),
                jackpot.getFixedContributionRate(),
                jackpot.getVariableInitialRate(),
                jackpot.getVariableMinRate(),
                jackpot.getVariableDecrementRate(),
                jackpot.getRewardType(),
                jackpot.getFixedRewardChance(),
                jackpot.getVariableInitialChance(),
                jackpot.getVariableCoefficient(),
                jackpot.getVariableLimitPool(),
                jackpot.getVersion());
    }

    private static ContributionData toContributionData(JackpotContribution contribution) {

        return new ContributionData(
                contribution.getBetId(),
                contribution.getUserId(),
                contribution.getJackpotId(),
                contribution.getStakeAmount(),
                contribution.getContributionAmount(),
                contribution.getCurrentJackpotAmount(),
                contribution.getCreatedAt());
    }

    private static final class BetLock {

        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger holders = new AtomicInteger(1);

        private void lock() {

            lock.lock();
        }

        private void unlock() {

            lock.unlock();
        }

        private void retain() {

            holders.incrementAndGet();
        }

        private int release() {

            return holders.decrementAndGet();
        }
    }
}
