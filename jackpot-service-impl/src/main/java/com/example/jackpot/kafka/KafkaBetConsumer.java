package com.example.jackpot.kafka;

import com.example.jackpot.dto.Bet;
import com.example.jackpot.dto.ContributionResult;
import com.example.jackpot.service.ContributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Header;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBetConsumer {

    private final ContributionService contributionService;

    @KafkaListener(topics = "${app.kafka.topic}")
    public void consume(
            Bet bet,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info(
                "Consumed bet from Kafka: topic={}, partition={}, offset={}, betId={}, userId={}, jackpotId={}, amount={}",
                topic,
                partition,
                offset,
                bet.getBetId(),
                bet.getUserId(),
                bet.getJackpotId(),
                bet.getBetAmount());

        ContributionResult result = contributionService.contribute(bet);

        switch (result.getStatus()) {

            case CREATED -> log.info(
                    "Processed contribution: betId={}, jackpotId={}, contributionAmount={}, currentJackpotAmount={}",
                    bet.getBetId(),
                    bet.getJackpotId(),
                    result.getContribution().getContributionAmount(),
                    result.getContribution().getCurrentJackpotAmount());

            case DUPLICATE -> log.info(
                    "Ignored duplicate bet: betId={}, jackpotId={}, currentJackpotAmount={}",
                    bet.getBetId(),
                    bet.getJackpotId(),
                    result.getContribution().getCurrentJackpotAmount());

            case JACKPOT_NOT_FOUND -> log.warn(
                    "Skipping consumed bet because jackpot does not exist: betId={}, jackpotId={}, topic={}, partition={}, offset={}",
                    bet.getBetId(),
                    bet.getJackpotId(),
                    topic,
                    partition,
                    offset);
        }
    }
}
