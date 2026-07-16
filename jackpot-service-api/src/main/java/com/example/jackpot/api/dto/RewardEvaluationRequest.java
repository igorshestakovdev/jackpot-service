package com.example.jackpot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to request reward evaluation for a bet")
public class RewardEvaluationRequest {

    @Schema(description = "Unique identifier of the bet to evaluate")
    @NotNull
    private UUID betId;

    @Schema(description = "Unique identifier of the user")
    @NotBlank
    @Size(max = 64)
    private String userId;

    @Schema(description = "Unique identifier of the jackpot")
    @NotBlank
    @Size(max = 64)
    private String jackpotId;
}
