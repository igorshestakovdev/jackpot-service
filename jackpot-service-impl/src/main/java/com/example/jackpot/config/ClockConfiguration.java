package com.example.jackpot.config;

import com.example.jackpot.service.RandomProvider;
import com.example.jackpot.utils.SecureRandomProvider;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean(RandomProvider.class)
    RandomProvider randomProvider() {
        return new SecureRandomProvider();
    }
}
