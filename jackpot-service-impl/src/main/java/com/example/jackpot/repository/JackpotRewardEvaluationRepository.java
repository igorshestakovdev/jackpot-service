package com.example.jackpot.repository;

import com.example.jackpot.entity.JackpotRewardEvaluation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRewardEvaluationRepository extends CrudRepository<JackpotRewardEvaluation, Long> {

    Optional<JackpotRewardEvaluation> findByBetId(UUID betId);
}
