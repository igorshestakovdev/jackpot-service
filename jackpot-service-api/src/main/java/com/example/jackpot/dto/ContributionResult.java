package com.example.jackpot.dto;

import com.example.jackpot.enums.ContributionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributionResult {

    private ContributionStatus status;

    private ContributionData contribution;
}
