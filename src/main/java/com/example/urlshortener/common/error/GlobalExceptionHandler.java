package com.example.urlshortener.common.error;

import com.example.urlshortener.common.web.CorrelationIdFilter;
import com.example.urlshortener.url.application.CustomAliasAlreadyExistsException;
import com.example.urlshortener.url.application.ReservedCustomAliasException;
import com.example.urlshortener.url.application.ShortCodeAllocationException;
import com.example.urlshortener.url.application.ShortUrlNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldViolation> violations = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldViolation(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed.",
                request,
                violations,
                new HttpHeaders()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldViolation> violations = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed.",
                request,
                violations,
                new HttpHeaders()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request body is missing or malformed.",
                request,
                List.of(),
                new HttpHeaders()
        );
    }

    @ExceptionHandler(ReservedCustomAliasException.class)
    public ResponseEntity<ApiErrorResponse> handleReservedAlias(
            ReservedCustomAliasException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.RESERVED_CUSTOM_ALIAS,
                exception.getMessage(),
                request,
                List.of(),
                new HttpHeaders()
        );
    }

    @ExceptionHandler(CustomAliasAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAlias(
            CustomAliasAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                ErrorCode.CUSTOM_ALIAS_ALREADY_EXISTS,
                exception.getMessage(),
                request,
                List.of(),
                new HttpHeaders()
        );
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ShortUrlNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.SHORT_URL_NOT_FOUND,
                exception.getMessage(),
                request,
                List.of(),
                new HttpHeaders()
        );
    }

    @ExceptionHandler(ShortCodeAllocationException.class)
    public ResponseEntity<ApiErrorResponse> handleAllocationFailure(
            ShortCodeAllocationException exception,
            HttpServletRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, "1");

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCode.SHORT_CODE_ALLOCATION_FAILED,
                exception.getMessage(),
                request,
                List.of(),
                headers
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseFailure(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Database operation failed", exception);

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCode.DEPENDENCY_UNAVAILABLE,
                "A required dependency is unavailable.",
                request,
                List.of(),
                new HttpHeaders()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedFailure(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Unexpected request failure", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred.",
                request,
                List.of(),
                new HttpHeaders()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<FieldViolation> violations,
            HttpHeaders headers
    ) {
        Object correlationValue =
                request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode.name(),
                message,
                request.getRequestURI(),
                correlationValue == null ? null : correlationValue.toString(),
                violations
        );

        return new ResponseEntity<>(response, headers, status);
    }
}
