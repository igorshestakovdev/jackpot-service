package com.example.jackpot.repository;

import com.example.jackpot.entity.JackpotReward;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRewardRepository extends CrudRepository<JackpotReward, Long> {

    Optional<JackpotReward> findByBetId(UUID betId);

    long countByJackpotId(String jackpotId);
}
