package com.example.jackpot.service.impl;

import com.example.jackpot.dto.RewardDecision;
import com.example.jackpot.dto.RewardEvaluationCommand;
import com.example.jackpot.dto.RewardEvaluationResult;
import com.example.jackpot.entity.Jackpot;
import com.example.jackpot.entity.JackpotContribution;
import com.example.jackpot.entity.JackpotReward;
import com.example.jackpot.entity.JackpotRewardEvaluation;
import com.example.jackpot.exception.ContributionMismatchException;
import com.example.jackpot.exception.ContributionNotFoundException;
import com.example.jackpot.exception.JackpotNotFoundException;
import com.example.jackpot.repository.JackpotContributionRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.JackpotRewardEvaluationRepository;
import com.example.jackpot.repository.JackpotRewardRepository;
import com.example.jackpot.service.RandomProvider;
import com.example.jackpot.service.RewardService;
import com.example.jackpot.utils.JackpotMathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final JackpotRepository jackpotRepository;

    private final JackpotContributionRepository contributionRepository;

    private final JackpotRewardRepository rewardRepository;

    private final JackpotRewardEvaluationRepository evaluationRepository;

    private final RandomProvider randomProvider;

    private final Clock clock;

    private final TransactionTemplate transactionTemplate;


    @Override
    public RewardEvaluationResult evaluate(RewardEvaluationCommand command) {

        JackpotContribution contribution = contributionRepository
                .findByBetId(command.getBetId())
                .orElseThrow(() -> new ContributionNotFoundException(command.getBetId()));

        validateOwnership(command, contribution);

        JackpotRewardEvaluation existingEvaluation = evaluationRepository.findByBetId(command.getBetId()).orElse(null);

        if (existingEvaluation != null) {
            log.info(
                    "Returning existing jackpot evaluation: betId={}, jackpotId={}, won={}, rewardAmount={}, evaluatedChance={}",
                    existingEvaluation.getBetId(),
                    existingEvaluation.getJackpotId(),
                    existingEvaluation.isWon(),
                    existingEvaluation.getRewardAmount(),
                    existingEvaluation.getEvaluatedChance());
            return toResult(existingEvaluation);
        }

        return transactionTemplate.execute(status -> {
            Jackpot jackpot = jackpotRepository
                    .findByIdForUpdate(command.getJackpotId())
                    .orElseThrow(() -> new JackpotNotFoundException(command.getJackpotId()));

            return evaluationRepository
                    .findByBetId(command.getBetId())
                    .map(RewardServiceImpl::toResult)
                    .orElseGet(() -> performEvaluation(command, jackpot));
        });
    }

    private RewardEvaluationResult performEvaluation(RewardEvaluationCommand command, Jackpot jackpot) {

        BigDecimal randomValue = randomProvider.nextValue();

        RewardDecision decision = JackpotMathUtils.evaluateReward(jackpot, randomValue);

        log.info(
                "Evaluating jackpot reward: betId={}, jackpotId={}, currentPool={}, evaluatedChance={}, randomValue={}",
                command.getBetId(),
                jackpot.getId(),
                jackpot.getCurrentPool(),
                decision.getChance(),
                decision.getRandomValue());

        Instant evaluatedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);

        BigDecimal rewardAmount = null;

        if (decision.isWon()) {

            rewardAmount = jackpot.getCurrentPool();

            jackpotRepository.save(withCurrentPool(jackpot, jackpot.getInitialPool()));

            log.info(
                    "Jackpot won and reset: betId={}, jackpotId={}, rewardAmount={}, resetPool={}",
                    command.getBetId(),
                    jackpot.getId(),
                    rewardAmount,
                    jackpot.getInitialPool());

            rewardRepository.save(

                    new JackpotReward(
                            null,
                            command.getBetId(),
                            command.getUserId(),
                            command.getJackpotId(),
                            rewardAmount,
                            evaluatedAt
                    )
            );
        } else {
            log.info(
                    "Jackpot not won: betId={}, jackpotId={}, currentPool={}, evaluatedChance={}, randomValue={}",
                    command.getBetId(),
                    jackpot.getId(),
                    jackpot.getCurrentPool(),
                    decision.getChance(),
                    decision.getRandomValue());
        }

        JackpotRewardEvaluation evaluation = new JackpotRewardEvaluation(
                null,
                command.getBetId(),
                command.getUserId(),
                command.getJackpotId(),
                decision.isWon(),
                decision.getChance(),
                decision.getRandomValue(),
                rewardAmount,
                evaluatedAt);

        return toResult(evaluationRepository.save(evaluation));
    }

    private static void validateOwnership(RewardEvaluationCommand command, JackpotContribution contribution) {

        if (!contribution.getUserId().equals(command.getUserId())
                || !contribution.getJackpotId().equals(command.getJackpotId())) {
            throw new ContributionMismatchException();
        }
    }

    private static RewardEvaluationResult toResult(JackpotRewardEvaluation evaluation) {

        return new RewardEvaluationResult(
                evaluation.getBetId(),
                evaluation.getUserId(),
                evaluation.getJackpotId(),
                evaluation.isWon(),
                evaluation.getRewardAmount(),
                evaluation.getEvaluatedChance(),
                evaluation.getCreatedAt());
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
}
