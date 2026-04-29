package com.emekachukwulobe.flightbookingservice.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.emekachukwulobe.flightbookingservice.dto.response.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.groupingBy(
                FieldError::getField,
                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
            ));
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
            .status(400).error("Validation Failed")
            .message("One or more fields are invalid")
            .validationErrors(errors)
            .timestamp(OffsetDateTime.now()).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(error(400, "Validation Failed", ex.getMessage()));
    }

    @ExceptionHandler({TenantNotFoundException.class, FlightNotFoundException.class,
        BookingNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler({DuplicateResourceException.class, InsufficientSeatsException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(PasswordReuseException.class)
    public ResponseEntity<ErrorResponse> handlePasswordReuse(PasswordReuseException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(error(422, "Password Policy Violation", ex.getMessage()));
    }

    @ExceptionHandler({PaymentException.class, BookingException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(error(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(403, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(401, "Unauthorized", "Invalid credentials"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error(500, "Internal Server Error", "An unexpected error occurred"));
    }

    private ErrorResponse error(int status, String error, String message) {
        return ErrorResponse.builder()
            .status(status).error(error).message(message)
            .timestamp(OffsetDateTime.now()).build();
    }
}
