package com.example.jackpot.integration.core;

import com.example.jackpot.utils.ControllableRandomProvider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class DeterministicRandomTestConfiguration {

    @Bean
    @Primary
    ControllableRandomProvider controllableRandomProvider() {

        return new ControllableRandomProvider();
    }
}
