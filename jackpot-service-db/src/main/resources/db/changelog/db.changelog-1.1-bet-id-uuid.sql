--liquibase formatted sql

--changeset jackpot-service:4
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'jackpot_contributions' AND column_name = 'bet_id' AND LOWER(data_type) = 'character varying'

ALTER TABLE jackpot_reward_evaluations DROP CONSTRAINT fk_evaluation_contribution;

ALTER TABLE jackpot_contributions ALTER COLUMN bet_id UUID;
ALTER TABLE jackpot_rewards ALTER COLUMN bet_id UUID;
ALTER TABLE jackpot_reward_evaluations ALTER COLUMN bet_id UUID;

ALTER TABLE jackpot_reward_evaluations
    ADD CONSTRAINT fk_evaluation_contribution FOREIGN KEY (bet_id) REFERENCES jackpot_contributions(bet_id);

--rollback ALTER TABLE jackpot_reward_evaluations DROP CONSTRAINT fk_evaluation_contribution;
--rollback ALTER TABLE jackpot_reward_evaluations ALTER COLUMN bet_id VARCHAR(64);
--rollback ALTER TABLE jackpot_rewards ALTER COLUMN bet_id VARCHAR(64);
--rollback ALTER TABLE jackpot_contributions ALTER COLUMN bet_id VARCHAR(64);
--rollback ALTER TABLE jackpot_reward_evaluations
--rollback     ADD CONSTRAINT fk_evaluation_contribution FOREIGN KEY (bet_id) REFERENCES jackpot_contributions(bet_id);
