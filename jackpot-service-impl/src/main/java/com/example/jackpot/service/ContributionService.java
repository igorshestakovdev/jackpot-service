package com.example.jackpot.service;

import com.example.jackpot.dto.Bet;
import com.example.jackpot.dto.ContributionResult;

public interface ContributionService {

    ContributionResult contribute(Bet bet);
}
