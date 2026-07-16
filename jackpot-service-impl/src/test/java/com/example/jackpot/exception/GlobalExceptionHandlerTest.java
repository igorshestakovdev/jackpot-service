package com.example.jackpot.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jackpot.api.dto.ApiError;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.core.MethodParameter;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsValidationErrorsToBadRequestResponse() throws NoSuchMethodException {

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Payload(), "payload");
        bindingResult.addError(new FieldError("payload", "betId", "must not be blank"));
        bindingResult.addError(new FieldError("payload", "userId", "must not be blank"));

        MethodParameter parameter = new MethodParameter(
                SampleController.class.getDeclaredMethod("handle", Payload.class),
                0);

        var response = handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation failed");
        assertThat(response.getBody().getFieldErrors())
                .extracting(ApiError.FieldError::getField, ApiError.FieldError::getMessage)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("betId", "must not be blank"),
                        org.assertj.core.groups.Tuple.tuple("userId", "must not be blank"));
    }

    @Test
    void mapsMalformedRequestToBadRequestResponse() {

        var response = handler.handleMalformedRequest(
                new HttpMessageNotReadableException("bad body", new MockHttpInputMessage(new byte[0])));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo("The request body is invalid");
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    @Test
    void mapsJackpotNotFoundToNotFoundResponse() {

        var response = handler.handleJackpotNotFound(new JackpotNotFoundException("jackpot-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("JACKPOT_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Jackpot not found: jackpot-1");
    }

    @Test
    void mapsContributionMismatchToConflictResponse() {

        var response = handler.handleContributionMismatch(new ContributionMismatchException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CONTRIBUTION_MISMATCH");
        assertThat(response.getBody().getMessage())
                .isEqualTo("The supplied userId or jackpotId does not match the processed bet");
    }

    @Test
    void mapsPublishingFailureToServiceUnavailableResponse() {

        var response = handler.handlePublishingFailure(
                new BetPublishingException("boom", new IllegalStateException("kafka")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("KAFKA_UNAVAILABLE");
        assertThat(response.getBody().getMessage()).isEqualTo("The bet could not be published; retry later");
    }

    @Test
    void mapsUnexpectedFailureToInternalServerErrorResponse() {

        var response = handler.handleUnexpectedFailure(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getFieldErrors()).isEqualTo(List.of());
    }

    private static final class SampleController {

        @SuppressWarnings("unused")
        void handle(Payload payload) {
        }
    }

    @SuppressWarnings("unused")
    private static final class Payload {
    }
}
