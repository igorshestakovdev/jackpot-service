package com.example.jackpot.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.experimental.UtilityClass;

/**
 * Provides common validation helpers for identifiers, amounts, and rates.
 */
@UtilityClass
public class ValidationUtils {

    private static final int MAX_ID_LENGTH = 64;

    /**
     * Validates identifier text, trims it, and enforces length limits.
     */
    public static String requireIdentifier(String value, String fieldName) {

        Objects.requireNonNull(value, fieldName + " must not be null");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        if (normalized.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException(fieldName + " must contain at most " + MAX_ID_LENGTH + " characters");
        }

        return normalized;
    }

    /**
     * Validates that the UUID field is present.
     */
    public static UUID requireUuid(UUID value, String fieldName) {

        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    /**
     * Validates a positive monetary amount and normalizes it to 2 decimals.
     */
    public static BigDecimal requireNormalizedAmount(BigDecimal value, String fieldName) {

        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }

        if (value.scale() > 2) {
            throw new IllegalArgumentException(fieldName + " must have at most 2 decimal places");
        }

        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * Validates that the rate is within the range from 0 to 1.
     */
    public static BigDecimal requireRate(BigDecimal value, String name) {

        requireNonNegative(value, name);

        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException(name + " must not exceed 1");
        }

        return value;
    }

    /**
     * Validates that the value is present and not negative.
     */
    public static BigDecimal requireNonNegative(BigDecimal value, String name) {

        if (value == null) {
            throw new IllegalStateException(name + " is required");
        }

        if (value.signum() < 0) {
            throw new IllegalStateException(name + " must not be negative");
        }

        return value;
    }
}
