package com.example.jackpot.integration.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jackpot.integration.core.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApiErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void returnsStructuredValidationAndNotFoundErrors() throws Exception {

        mockMvc
                .perform(
                        post("/api/v1/bets")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "betId": null,
                                          "userId": "user",
                                          "jackpotId": "fixed-jackpot",
                                          "betAmount": 0.001
                                        }
                                        """)
                )

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        mockMvc
                .perform(
                        post("/api/v1/jackpots/evaluate")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "betId": "29c0f552-86d4-4e12-9860-d7f5278054f9",
                                          "userId": "user",
                                          "jackpotId": "fixed-jackpot"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTRIBUTION_NOT_FOUND"));
    }
}
