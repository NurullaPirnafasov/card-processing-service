package com.example.cardprocessingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("not_found", ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimitExceeded(LimitExceededException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("limit_exceeded", ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("invalid_data", ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("insufficient_funds", ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PreconditionFailedException.class)
    public ResponseEntity<Object> handlePreconditionFailed(PreconditionFailedException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("precondition_failed", ex.getMessage()),
                HttpStatus.PRECONDITION_FAILED);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<String> missingFields = fieldErrors.stream()
                .filter(f -> "NotNull".equals(f.getCode()) || "NotBlank".equals(f.getCode()))
                .map(FieldError::getField)
                .collect(Collectors.toList());

        List<String> invalidFields = fieldErrors.stream()
                .filter(f -> !"NotNull".equals(f.getCode()) && !"NotBlank".equals(f.getCode()))
                .map(FieldError::getField)
                .collect(Collectors.toList());

        if (!missingFields.isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorResponse("missing_field", "Missing required field(s): " + String.join(", ", missingFields)),
                    HttpStatus.BAD_REQUEST
            );
        } else {
            return new ResponseEntity<>(
                    new ErrorResponse("invalid_data", "Invalid value(s) for field(s): " + String.join(", ", invalidFields)),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return new ResponseEntity<>(
                new ErrorResponse("internal_error", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
