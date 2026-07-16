package com.example.jackpot.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardDecision {

    private boolean won;

    private BigDecimal chance;

    private BigDecimal randomValue;
}
