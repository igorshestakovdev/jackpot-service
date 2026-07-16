package com.example.jackpot.utils;

import com.example.jackpot.service.RandomProvider;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Deterministic random provider that lets integration tests assert idempotency precisely.
 */
public final class ControllableRandomProvider implements RandomProvider {

    private final AtomicReference<BigDecimal> value = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicInteger invocationCount = new AtomicInteger();

    @Override
    /** Returns the configured deterministic random value for the current test. */
    public BigDecimal nextValue() {

        invocationCount.incrementAndGet();
        return value.get();
    }

    /** Replaces the deterministic value and clears invocation counters between tests. */
    public void reset(BigDecimal newValue) {

        value.set(newValue);
        invocationCount.set(0);
    }

    /** Exposes how many random draws happened during the scenario. */
    public int invocationCount() {

        return invocationCount.get();
    }
}
