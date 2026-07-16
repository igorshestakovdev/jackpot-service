package com.example.jackpot.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
@ConfigurationProperties("app.kafka")
public class JackpotKafkaProperties {

    @NotBlank
    private String topic;

    @NotBlank
    private String deadLetterTopic;

    @Min(1)
    private int partitions;

    @NotNull
    private Duration publishTimeout;

    @NotNull
    @Valid
    private Embedded embedded;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedded {

        private boolean enabled;

        @Min(0)
        @Max(65535)
        private int port;
    }
}
