package com.example.jackpot.repository;

import com.example.jackpot.entity.JackpotContribution;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotContributionRepository extends CrudRepository<JackpotContribution, Long> {

    Optional<JackpotContribution> findByBetId(UUID betId);

    long countByJackpotId(String jackpotId);
}
