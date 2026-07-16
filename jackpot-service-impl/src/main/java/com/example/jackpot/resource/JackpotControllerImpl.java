package com.example.jackpot.resource;

import com.example.jackpot.api.dto.RewardEvaluationRequest;
import com.example.jackpot.api.dto.RewardEvaluationResponse;
import com.example.jackpot.api.controller.JackpotController;
import com.example.jackpot.dto.RewardEvaluationCommand;
import com.example.jackpot.dto.RewardEvaluationResult;
import com.example.jackpot.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JackpotControllerImpl implements JackpotController {

    private final RewardService rewardService;

    @Override
    public ResponseEntity<RewardEvaluationResponse> evaluate(RewardEvaluationRequest request) {

        RewardEvaluationCommand command = new RewardEvaluationCommand(
                request.getBetId(),
                request.getUserId(),
                request.getJackpotId()
        );

        RewardEvaluationResult result = rewardService.evaluate(command);

        return ResponseEntity.ok(new RewardEvaluationResponse(
                result.getBetId(),
                result.getUserId(),
                result.getJackpotId(),
                result.isWon(),
                result.getRewardAmount(),
                result.getEvaluatedChance(),
                result.getEvaluatedAt()
        ));
    }
}
