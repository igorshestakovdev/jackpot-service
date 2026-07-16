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
public class ContributionData {

    private UUID betId;

    private String userId;

    private String jackpotId;

    private BigDecimal betAmount;

    private BigDecimal contributionAmount;

    private BigDecimal currentJackpotAmount;

    private Instant createdAt;
}
