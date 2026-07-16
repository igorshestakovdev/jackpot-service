package com.example.jackpot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API error response structure")
public class ApiError {

    @Schema(description = "Error code identifying the category of the error")
    private String code;

    @Schema(description = "Human-readable description of the error")
    private String message;

    @Schema(description = "HTTP status code")
    private Integer status;

    @Schema(description = "Timestamp of when the error occurred")
    private Instant timestamp;

    @Schema(description = "List of field-specific validation errors")
    private List<FieldError> fieldErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Validation error detail for a specific field")
    public static class FieldError {

        @Schema(description = "Name of the invalid request field")
        private String field;

        @Schema(description = "Description of why the validation failed for the field")
        private String message;
    }
}
