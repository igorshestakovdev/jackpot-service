package com.example.jackpot.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardEvaluationResult {

    private UUID betId;

    private String userId;

    private String jackpotId;

    private boolean won;

    private BigDecimal rewardAmount;

    private BigDecimal evaluatedChance;

    private Instant evaluatedAt;
}
