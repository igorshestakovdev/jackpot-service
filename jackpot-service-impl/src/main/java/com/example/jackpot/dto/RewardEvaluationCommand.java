package com.example.jackpot.dto;

import com.example.jackpot.utils.ValidationUtils;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RewardEvaluationCommand {

    private UUID betId;

    private String userId;

    private String jackpotId;

    public RewardEvaluationCommand(UUID betId, String userId, String jackpotId) {

        this.betId = ValidationUtils.requireUuid(betId, "betId");
        this.userId = ValidationUtils.requireIdentifier(userId, "userId");
        this.jackpotId = ValidationUtils.requireIdentifier(jackpotId, "jackpotId");
    }
}
