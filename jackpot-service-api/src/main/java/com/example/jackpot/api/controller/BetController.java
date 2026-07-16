package com.example.jackpot.api.controller;

import com.example.jackpot.api.dto.BetAcceptedResponse;
import com.example.jackpot.api.dto.PublishBetRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/bets")
public interface BetController {

    @PostMapping
    ResponseEntity<BetAcceptedResponse> publish(@Valid @RequestBody PublishBetRequest request);
}
