package com.example.jackpot.resource;

import com.example.jackpot.api.dto.BetAcceptedResponse;
import com.example.jackpot.api.dto.PublishBetRequest;
import com.example.jackpot.api.controller.BetController;
import com.example.jackpot.dto.Bet;
import com.example.jackpot.service.BetPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BetControllerImpl implements BetController {

    private final BetPublisher betPublisher;

    @Override
    public ResponseEntity<BetAcceptedResponse> publish(PublishBetRequest request) {

        Bet bet = new Bet(
                request.getBetId(),
                request.getUserId(),
                request.getJackpotId(),
                request.getBetAmount()
        );

        betPublisher.publish(bet);

        return ResponseEntity
                .accepted()
                .body(
                        new BetAcceptedResponse(bet.getBetId(), "ACCEPTED")
                );
    }
}
