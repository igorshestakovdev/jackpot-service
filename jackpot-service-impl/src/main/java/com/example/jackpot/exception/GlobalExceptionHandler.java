package com.example.jackpot.exception;

import com.example.jackpot.api.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {

        List<ApiError.FieldError> fieldErrors =

                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
                        .toList();

        return
                error(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_FAILED",
                        "Request validation failed",
                        fieldErrors
                );
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> handleMalformedRequest(Exception exception) {

        log.debug("Malformed request", exception);

        return
                error(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_REQUEST",
                        "The request body is invalid",
                        List.of()
                );
    }

    @ExceptionHandler(ContributionNotFoundException.class)
    ResponseEntity<ApiError> handleContributionNotFound(ContributionNotFoundException exception) {

        return
                error(
                        HttpStatus.NOT_FOUND,
                        "CONTRIBUTION_NOT_FOUND",
                        exception.getMessage(),
                        List.of()
                );
    }

    @ExceptionHandler(JackpotNotFoundException.class)
    ResponseEntity<ApiError> handleJackpotNotFound(JackpotNotFoundException exception) {

        return
                error(
                        HttpStatus.NOT_FOUND,
                        "JACKPOT_NOT_FOUND",
                        exception.getMessage(),
                        List.of()
                );
    }

    @ExceptionHandler(ContributionMismatchException.class)
    ResponseEntity<ApiError> handleContributionMismatch(ContributionMismatchException exception) {

        return
                error(
                        HttpStatus.CONFLICT,
                        "CONTRIBUTION_MISMATCH",
                        exception.getMessage(),
                        List.of()
                );
    }

    @ExceptionHandler(BetPublishingException.class)
    ResponseEntity<ApiError> handlePublishingFailure(BetPublishingException exception) {

        log.error("Unable to publish bet", exception);

        return
                error(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "KAFKA_UNAVAILABLE",
                        "The bet could not be published; retry later",
                        List.of()
                );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpectedFailure(Exception exception) {

        log.error("Unexpected request processing failure", exception);

        return
                error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR",
                        "An unexpected error occurred",
                        List.of()
                );
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message, List<ApiError.FieldError> fieldErrors) {

        return ResponseEntity
                .status(status)
                .body(
                        new ApiError(
                                code,
                                message,
                                status.value(),
                                Instant.now(),
                                List.copyOf(fieldErrors)
                        )
                );
    }

}
