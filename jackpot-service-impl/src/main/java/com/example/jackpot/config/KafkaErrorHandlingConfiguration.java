package com.example.jackpot.config;

import com.example.jackpot.dto.Bet;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaErrorHandlingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfiguration.class);

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Bet> kafkaTemplate, JackpotKafkaProperties properties) {

        DeadLetterPublishingRecoverer recoverer =

                new DeadLetterPublishingRecoverer(

                        kafkaTemplate,

                        (record, exception) -> {
                            log.error(
                                    "Sending Kafka record to dead-letter topic: sourceTopic={}, sourcePartition={}, offset={}, dltTopic={}, key={}, payload={}",
                                    record.topic(),
                                    record.partition(),
                                    record.offset(),
                                    properties.getDeadLetterTopic(),
                                    record.key(),
                                    record.value(),
                                    exception);
                            return new TopicPartition(properties.getDeadLetterTopic(), record.partition());
                        }
                );

        recoverer.setFailIfSendResultIsError(true);

        return new DefaultErrorHandler(recoverer, new FixedBackOff(250L, 2L));
    }
}
