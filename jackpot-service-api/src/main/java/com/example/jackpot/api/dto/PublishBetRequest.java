package com.example.jackpot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to publish a new bet to the jackpot system")
public class PublishBetRequest {

    @Schema(description = "Unique identifier of the bet")
    @NotNull
    private UUID betId;

    @Schema(description = "Unique identifier of the user who placed the bet")
    @NotBlank
    @Size(max = 64)
    private String userId;

    @Schema(description = "Unique identifier of the jackpot target")
    @NotBlank
    @Size(max = 64)
    private String jackpotId;

    @Schema(description = "Amount of money placed on the bet")
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 17, fraction = 2)
    private BigDecimal betAmount;
}
