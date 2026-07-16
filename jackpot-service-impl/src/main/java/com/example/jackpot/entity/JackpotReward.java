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
@Table("jackpot_rewards")
public class JackpotReward {

    @Id
    @Column("id")
    private Long id;

    @Column("bet_id")
    private UUID betId;

    @Column("user_id")
    private String userId;

    @Column("jackpot_id")
    private String jackpotId;

    @Column("jackpot_reward_amount")
    private BigDecimal jackpotRewardAmount;

    @Column("created_at")
    private Instant createdAt;
}
