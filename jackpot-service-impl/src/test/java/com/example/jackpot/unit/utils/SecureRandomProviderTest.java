package com.example.jackpot.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.utils.SecureRandomProvider;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SecureRandomProviderTest {

    @Test
    void returnsValueWithinHalfOpenUnitInterval() {

        BigDecimal value = new SecureRandomProvider().nextValue();

        assertThat(value).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(value).isLessThan(BigDecimal.ONE);
    }
}
