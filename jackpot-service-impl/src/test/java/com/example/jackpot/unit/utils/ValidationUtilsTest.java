package com.example.jackpot.unit.utils;

import static com.example.jackpot.utils.TestUtils.amount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ValidationUtilsTest {

    @Test
    void trimsAndReturnsIdentifier() {

        assertThat(ValidationUtils.requireIdentifier("  user-1  ", "userId"))
                .isEqualTo("user-1");
    }

    @Test
    void rejectsBlankIdentifier() {

        assertThatThrownBy(() -> ValidationUtils.requireIdentifier("   ", "userId"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId must not be blank");
    }

    @Test
    void rejectsTooLongIdentifier() {

        String value = "x".repeat(65);

        assertThatThrownBy(() -> ValidationUtils.requireIdentifier(value, "userId"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId must contain at most 64 characters");
    }

    @Test
    void rejectsNullUuid() {

        assertThatThrownBy(() -> ValidationUtils.requireUuid(null, "betId"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("betId must not be null");
    }

    @Test
    void returnsUuidAsIs() {

        UUID value = UUID.randomUUID();

        assertThat(ValidationUtils.requireUuid(value, "betId")).isEqualTo(value);
    }

    @Test
    void rejectsRateAboveOne() {

        assertThatThrownBy(() -> ValidationUtils.requireRate(amount("1.01"), "chance"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chance must not exceed 1");
    }

    @Test
    void rejectsNullNonNegativeValue() {

        assertThatThrownBy(() -> ValidationUtils.requireNonNegative(null, "chance"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chance is required");
    }

    @Test
    void rejectsNegativeNonNegativeValue() {

        assertThatThrownBy(() -> ValidationUtils.requireNonNegative(amount("-0.01"), "chance"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chance must not be negative");
    }
}
