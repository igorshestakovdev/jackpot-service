package com.example.jackpot.kafka;

import com.example.jackpot.config.JackpotKafkaProperties;
import com.example.jackpot.dto.Bet;
import com.example.jackpot.exception.BetPublishingException;
import com.example.jackpot.service.BetPublisher;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBetPublisher implements BetPublisher {

    private final KafkaTemplate<String, Bet> kafkaTemplate;

    private final JackpotKafkaProperties properties;

    @Override
    public void publish(Bet bet) {

        try {

            log.info(
                    "Publishing bet to Kafka: topic={}, key={}, betId={}, userId={}, jackpotId={}, amount={}",
                    properties.getTopic(),
                    bet.getJackpotId(),
                    bet.getBetId(),
                    bet.getUserId(),
                    bet.getJackpotId(),
                    bet.getBetAmount());

            SendResult<String, Bet> sendResult = kafkaTemplate
                    .send(properties.getTopic(), bet.getJackpotId(), bet)
                    .get(properties.getPublishTimeout().toMillis(), TimeUnit.MILLISECONDS);

            if (sendResult != null && sendResult.getRecordMetadata() != null) {
                log.info(
                        "Kafka acknowledged bet: topic={}, partition={}, offset={}, betId={}, jackpotId={}",
                        sendResult.getRecordMetadata().topic(),
                        sendResult.getRecordMetadata().partition(),
                        sendResult.getRecordMetadata().offset(),
                        bet.getBetId(),
                        bet.getJackpotId());
            } else {
                log.info(
                        "Kafka acknowledged bet without record metadata: topic={}, betId={}, jackpotId={}",
                        properties.getTopic(),
                        bet.getBetId(),
                        bet.getJackpotId());
            }

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();
            log.error(
                    "Interrupted while publishing bet to Kafka: betId={}, jackpotId={}",
                    bet.getBetId(),
                    bet.getJackpotId(),
                    exception);

            throw new BetPublishingException("Publishing the bet was interrupted", exception);

        } catch (ExecutionException | TimeoutException exception) {

            log.error(
                    "Kafka did not acknowledge bet: topic={}, betId={}, jackpotId={}",
                    properties.getTopic(),
                    bet.getBetId(),
                    bet.getJackpotId(),
                    exception);
            throw new BetPublishingException("Kafka did not acknowledge the bet", exception);

        }
    }
}
