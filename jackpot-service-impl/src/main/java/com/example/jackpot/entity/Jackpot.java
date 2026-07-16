package com.example.jackpot.entity;

import com.example.jackpot.enums.ContributionType;
import com.example.jackpot.enums.RewardType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table("jackpots")
public class Jackpot {

    @Id
    @Column("id")
    private String id;

    @Column("name")
    private String name;

    @Column("current_pool")
    private BigDecimal currentPool;

    @Column("initial_pool")
    private BigDecimal initialPool;

    @Column("contribution_type")
    private ContributionType contributionType;

    @Column("fixed_contribution_rate")
    private BigDecimal fixedContributionRate;

    @Column("variable_initial_rate")
    private BigDecimal variableInitialRate;

    @Column("variable_min_rate")
    private BigDecimal variableMinRate;

    @Column("variable_decrement_rate")
    private BigDecimal variableDecrementRate;

    @Column("reward_type")
    private RewardType rewardType;

    @Column("fixed_reward_chance")
    private BigDecimal fixedRewardChance;

    @Column("variable_initial_chance")
    private BigDecimal variableInitialChance;

    @Column("variable_coefficient")
    private BigDecimal variableCoefficient;

    @Column("variable_limit_pool")
    private BigDecimal variableLimitPool;

    @Version
    @Column("version")
    private Long version;
}
