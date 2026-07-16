package com.example.jackpot.dto;

import com.example.jackpot.utils.ValidationUtils;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Bet {

    private UUID betId;

    private String userId;

    private String jackpotId;

    private BigDecimal betAmount;

    public Bet(UUID betId, String userId, String jackpotId, BigDecimal betAmount) {

        this.betId = ValidationUtils.requireUuid(betId, "betId");
        this.userId = ValidationUtils.requireIdentifier(userId, "userId");
        this.jackpotId = ValidationUtils.requireIdentifier(jackpotId, "jackpotId");
        this.betAmount = ValidationUtils.requireNormalizedAmount(betAmount, "betAmount");
    }
}
