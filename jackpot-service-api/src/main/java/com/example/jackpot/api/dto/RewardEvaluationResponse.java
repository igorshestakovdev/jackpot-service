package com.example.jackpot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of evaluating a bet for a potential jackpot reward")
public class RewardEvaluationResponse {

    @Schema(description = "Unique identifier of the evaluated bet")
    private UUID betId;

    @Schema(description = "Unique identifier of the user")
    private String userId;

    @Schema(description = "Unique identifier of the jackpot")
    private String jackpotId;

    @Schema(description = "Flag indicating whether the bet won the jackpot reward")
    private Boolean won;

    @Schema(description = "The reward payout amount if won, otherwise zero")
    private BigDecimal rewardAmount;

    @Schema(description = "The calculated winning probability for the evaluation")
    private BigDecimal evaluatedChance;

    @Schema(description = "Timestamp when the reward evaluation took place")
    private Instant evaluatedAt;
}
