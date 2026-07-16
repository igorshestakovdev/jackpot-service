package com.example.jackpot.utils;

import com.example.jackpot.service.RandomProvider;
import java.math.BigDecimal;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Supplies cryptographically strong random values for reward evaluation.
 */
@Component
public final class SecureRandomProvider implements RandomProvider {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Returns the next random value in the range from 0 inclusive to 1 exclusive.
     */
    @Override
    public BigDecimal nextValue() {

        return BigDecimal.valueOf(secureRandom.nextDouble());
    }
}
