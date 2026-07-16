package com.example.jackpot.integration.core;

import com.example.jackpot.dto.Bet;
import com.example.jackpot.utils.ControllableRandomProvider;
import com.example.jackpot.repository.JackpotContributionRepository;
import com.example.jackpot.repository.JackpotRepository;
import com.example.jackpot.repository.JackpotRewardEvaluationRepository;
import com.example.jackpot.repository.JackpotRewardRepository;
import com.example.jackpot.service.ContributionService;
import com.example.jackpot.service.RewardService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared Spring test harness for API, Kafka and concurrency integration scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(DeterministicRandomTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    protected static final String FIXED_JACKPOT_ID = "fixed-jackpot";
    protected static final String VARIABLE_JACKPOT_ID = "variable-jackpot";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected KafkaTemplate<String, Bet> kafkaTemplate;

    @Autowired
    protected ContributionService contributionService;

    @Autowired
    protected RewardService rewardService;

    @Autowired
    protected JackpotRepository jackpotRepository;

    @Autowired
    protected JackpotContributionRepository contributionRepository;

    @Autowired
    protected JackpotRewardRepository rewardRepository;

    @Autowired
    protected JackpotRewardEvaluationRepository evaluationRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ControllableRandomProvider randomProvider;

    private final List<ExecutorService> executors = new ArrayList<>();

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {

        String databaseName = "jackpot-test-" + UUID.randomUUID();

        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:%s;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=10000"
                        .formatted(databaseName));

        registry.add("spring.kafka.consumer.group-id", () -> "jackpot-service-it-" + UUID.randomUUID());
    }

    @BeforeEach
    void resetDatabaseState() {

        jdbcTemplate.update("DELETE FROM jackpot_reward_evaluations");
        jdbcTemplate.update("DELETE FROM jackpot_rewards");
        jdbcTemplate.update("DELETE FROM jackpot_contributions");

        jdbcTemplate.update("UPDATE jackpots SET current_pool = initial_pool, version = 0");

        randomProvider.reset(BigDecimal.ZERO);
    }

    @AfterEach
    void shutDownExecutors() {

        executors.forEach(ExecutorService::shutdownNow);
        executors.clear();
    }

    protected ExecutorService newExecutor(int threadCount) {

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        executors.add(executor);

        return executor;
    }
}
