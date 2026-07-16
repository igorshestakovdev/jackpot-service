package com.example.jackpot.utils;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Produces compact JSON payloads so API tests stay focused on behavior.
 */
public final class ApiRequestFactory {

    private ApiRequestFactory() {}

    /** Creates a publish-bet request body for MVC integration tests. */
    public static String publishBetRequest(UUID betId, String userId, String jackpotId, BigDecimal betAmount) {

        return """
                {
                  "betId": "%s",
                  "userId": "%s",
                  "jackpotId": "%s",
                  "betAmount": %s
                }
                """.formatted(betId, userId, jackpotId, betAmount.toPlainString());
    }

    /** Creates a reward-evaluation request body for MVC integration tests. */
    public static String rewardEvaluationRequest(UUID betId, String userId, String jackpotId) {

        return """
                {
                  "betId": "%s",
                  "userId": "%s",
                  "jackpotId": "%s"
                }
                """.formatted(betId, userId, jackpotId);
    }
}
