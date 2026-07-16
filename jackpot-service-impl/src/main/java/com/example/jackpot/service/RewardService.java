package com.example.jackpot.service;

import com.example.jackpot.dto.RewardEvaluationCommand;
import com.example.jackpot.dto.RewardEvaluationResult;

public interface RewardService {

    RewardEvaluationResult evaluate(RewardEvaluationCommand command);
}
