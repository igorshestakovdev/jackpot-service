package com.example.jackpot.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table("jackpot_reward_evaluations")
public class JackpotRewardEvaluation {

    @Id
    @Column("id")
    private Long id;

    @Column("bet_id")
    private UUID betId;

    @Column("user_id")
    private String userId;

    @Column("jackpot_id")
    private String jackpotId;

    @Column("won")
    private boolean won;

    @Column("evaluated_chance")
    private BigDecimal evaluatedChance;

    @Column("random_value")
    private BigDecimal randomValue;

    @Column("reward_amount")
    private BigDecimal rewardAmount;

    @Column("created_at")
    private Instant createdAt;
}
