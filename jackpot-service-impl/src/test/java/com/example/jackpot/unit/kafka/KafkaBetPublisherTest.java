package com.example.jackpot.unit.kafka;

import static com.example.jackpot.utils.TestUtils.aBet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jackpot.config.JackpotKafkaProperties;
import com.example.jackpot.dto.Bet;
import com.example.jackpot.exception.BetPublishingException;
import com.example.jackpot.kafka.KafkaBetPublisher;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaBetPublisherTest {

    @Mock
    private KafkaTemplate<String, Bet> kafkaTemplate;

    private KafkaBetPublisher publisher;

    private JackpotKafkaProperties properties;

    @BeforeEach
    void setUp() {

        properties = new JackpotKafkaProperties(
                "jackpot-bets",
                "jackpot-bets-dlt",
                3,
                Duration.ofSeconds(1),
                new JackpotKafkaProperties.Embedded(true, 9092));

        publisher = new KafkaBetPublisher(kafkaTemplate, properties);
    }

    @Test
    void publishesBetWhenKafkaAcknowledgesMessage() {

        Bet bet = aBet();
        CompletableFuture<SendResult<String, Bet>> future = CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(properties.getTopic(), bet.getJackpotId(), bet)).thenReturn(future);

        assertThatCode(() -> publisher.publish(bet)).doesNotThrowAnyException();

        verify(kafkaTemplate).send(properties.getTopic(), bet.getJackpotId(), bet);
    }

    @Test
    void wrapsExecutionFailure() {

        Bet bet = aBet();
        CompletableFuture<SendResult<String, Bet>> future = new CompletableFuture<>();
        future.completeExceptionally(new IllegalStateException("boom"));

        when(kafkaTemplate.send(properties.getTopic(), bet.getJackpotId(), bet)).thenReturn(future);

        assertThatThrownBy(() -> publisher.publish(bet))
                .isInstanceOf(BetPublishingException.class)
                .hasMessage("Kafka did not acknowledge the bet")
                .hasCauseInstanceOf(ExecutionException.class);
    }

    @Test
    void restoresInterruptFlagAndWrapsInterruptedFailure() {

        Bet bet = aBet();
        CompletableFuture<SendResult<String, Bet>> future = new InterruptedFuture<>();

        when(kafkaTemplate.send(properties.getTopic(), bet.getJackpotId(), bet)).thenReturn(future);

        try {
            assertThatThrownBy(() -> publisher.publish(bet))
                    .isInstanceOf(BetPublishingException.class)
                    .hasMessage("Publishing the bet was interrupted")
                    .hasCauseInstanceOf(InterruptedException.class);

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void wrapsTimeoutFailure() {

        Bet bet = aBet();
        CompletableFuture<SendResult<String, Bet>> future = new TimeoutFuture<>();

        when(kafkaTemplate.send(properties.getTopic(), bet.getJackpotId(), bet)).thenReturn(future);

        assertThatThrownBy(() -> publisher.publish(bet))
                .isInstanceOf(BetPublishingException.class)
                .hasMessage("Kafka did not acknowledge the bet")
                .hasCauseInstanceOf(TimeoutException.class);
    }

    private static final class InterruptedFuture<T> extends CompletableFuture<T> {

        @Override
        public T get(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {

            throw new InterruptedException("interrupted");
        }
    }

    private static final class TimeoutFuture<T> extends CompletableFuture<T> {

        @Override
        public T get(long timeout, java.util.concurrent.TimeUnit unit) throws TimeoutException {

            throw new TimeoutException("timeout");
        }
    }
}
