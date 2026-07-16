--liquibase formatted sql

--changeset jackpot-service:0
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM jackpots WHERE id = 'fixed-jackpot'

-- =============================================================================
-- SEED DATA: Initial Jackpot Configurations
-- Description: Seeds the pre-configured jackpot pools ('fixed-jackpot' and 'variable-jackpot').
--              Since the application lacks administrative APIs to create/manage pools,
--              these database records act as the system configuration.
--              Required for both integration tests and out-of-the-box API functionality.
-- =============================================================================


INSERT INTO jackpots (
    id, name, current_pool, initial_pool, contribution_type,
    fixed_contribution_rate, variable_initial_rate, variable_min_rate, variable_decrement_rate,
    reward_type, fixed_reward_chance, variable_initial_chance, variable_coefficient,
    variable_limit_pool, version
) VALUES (
    'fixed-jackpot', 'Fixed Jackpot', 1000.00, 1000.00, 'FIXED',
    0.1000000000, NULL, NULL, NULL,
    'FIXED', 0.0010000000, NULL, NULL, NULL, 0
);

--rollback DELETE FROM jackpots WHERE id = 'fixed-jackpot';

--changeset jackpot-service:1
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM jackpots WHERE id = 'variable-jackpot'
INSERT INTO jackpots (
    id, name, current_pool, initial_pool, contribution_type,
    fixed_contribution_rate, variable_initial_rate, variable_min_rate, variable_decrement_rate,
    reward_type, fixed_reward_chance, variable_initial_chance, variable_coefficient,
    variable_limit_pool, version
) VALUES (
    'variable-jackpot', 'Variable Jackpot', 500.00, 500.00, 'VARIABLE',
    NULL, 0.2000000000, 0.0500000000, 0.0001000000,
    'VARIABLE', NULL, 0.0010000000, 0.0005000000, 2500.00, 0
);

--rollback DELETE FROM jackpots WHERE id = 'variable-jackpot';
