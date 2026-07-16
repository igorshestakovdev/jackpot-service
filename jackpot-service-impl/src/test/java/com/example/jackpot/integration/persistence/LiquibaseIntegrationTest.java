package com.example.jackpot.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.integration.core.AbstractIntegrationTest;

import org.junit.jupiter.api.Test;

class LiquibaseIntegrationTest extends AbstractIntegrationTest {

    @Test
    void appliesSchemaAndSeedsBothJackpotStrategies() {

        Integer migrations = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM databasechangelog", Integer.class);
        Integer jackpots = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM jackpots", Integer.class);

        assertThat(migrations).isEqualTo(7);
        assertThat(jackpots).isEqualTo(2);

        assertThat(jackpotRepository.findById(FIXED_JACKPOT_ID)).isPresent();
        assertThat(jackpotRepository.findById(VARIABLE_JACKPOT_ID)).isPresent();
    }
}
