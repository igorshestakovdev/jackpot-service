package com.example.jackpot.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.kafka.embedded", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddedKafkaConfiguration {

    @Bean(name = EmbeddedKafkaBroker.BEAN_NAME)
    EmbeddedKafkaBroker embeddedKafkaBroker(JackpotKafkaProperties properties) {

        EmbeddedKafkaZKBroker broker = new EmbeddedKafkaZKBroker(
                1,
                false,
                properties.getPartitions(),
                properties.getTopic(),
                properties.getDeadLetterTopic()
        );

        if (properties.getEmbedded().getPort() > 0) {
            broker.kafkaPorts(properties.getEmbedded().getPort());
        }
        broker.brokerListProperty("app.kafka.embedded.bootstrap-servers");
        broker.brokerProperties(Map.of(
                "auto.create.topics.enable", "false",
                "transaction.state.log.replication.factor", "1",
                "transaction.state.log.min.isr", "1"
        ));
        broker.adminTimeout(30);

        return broker;
    }

    @Bean
    KafkaConnectionDetails embeddedKafkaConnectionDetails(EmbeddedKafkaBroker broker) {

        List<String> bootstrapServers = Arrays.stream(broker.getBrokersAsString().split(","))
                .map(String::trim)
                .toList();

        return () -> bootstrapServers;
    }
}
