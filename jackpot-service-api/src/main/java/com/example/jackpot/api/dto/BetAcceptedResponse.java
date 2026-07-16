package com.example.jackpot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload when a bet is accepted for processing")
public class BetAcceptedResponse {

    @Schema(description = "Unique identifier of the accepted bet")
    private UUID betId;

    @Schema(description = "Processing status of the bet")
    private String status;
}
