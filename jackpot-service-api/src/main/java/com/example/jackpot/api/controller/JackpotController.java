package com.example.jackpot.api.controller;

import com.example.jackpot.api.dto.RewardEvaluationRequest;
import com.example.jackpot.api.dto.RewardEvaluationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/jackpots")
public interface JackpotController {

    @PostMapping("/evaluate")
    ResponseEntity<RewardEvaluationResponse> evaluate(@Valid @RequestBody RewardEvaluationRequest request);
}
