package com.example.jackpot.unit.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jackpot.dto.Bet;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BetTest {

    @Test
    void normalizesMoneyScale() {

        UUID betId = UUID.randomUUID();
        Bet bet = new Bet(betId, " user-1 ", " jackpot-1 ", new BigDecimal("10"));

        assertThat(bet.getBetId()).isEqualTo(betId);
        assertThat(bet.getUserId()).isEqualTo("user-1");
        assertThat(bet.getJackpotId()).isEqualTo("jackpot-1");
        assertThat(bet.getBetAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsNonPositiveAmount() {

        assertThatThrownBy(() -> new Bet(UUID.randomUUID(), "user", "jackpot", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("betAmount must be greater than zero");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {

        assertThatThrownBy(() -> new Bet(UUID.randomUUID(), "user", "jackpot", new BigDecimal("1.001")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("betAmount must have at most 2 decimal places");
    }

    @Test
    void rejectsNullBetId() {

        assertThatThrownBy(() -> new Bet(null, "user", "jackpot", BigDecimal.ONE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("betId must not be null");
    }
}
