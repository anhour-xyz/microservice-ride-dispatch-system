package com.rideshare.auth_service.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception){
        Map<String,String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.putIfAbsent(error.getField(),error.getDefaultMessage())
        );


        return response(HttpStatus.BAD_REQUEST, "Request validation failed", errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlredyExists(
        EmailAlreadyExistsException exception
    ){
        return response(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
        InvalidCredentialsException exception
    ){
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(),Map.of());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(
        InvalidRefreshTokenException exception
    ){
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiError> handleEmailNotVerified(
        EmailNotVerifiedException exception
    ){
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ApiError> handleInvalidVerificationToken(
        InvalidVerificationTokenException exception
    ){
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(VerificationTokenExpiredException.class)
    public ResponseEntity<ApiError> handleExpiredVerificationToken(
        VerificationTokenExpiredException exception
    ){
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }


    private ResponseEntity<ApiError> response(
        HttpStatus status,
        String message,
        Map<String, String> validationErrors
    ){
        return ResponseEntity.status(status).body(new ApiError(
            Instant.now(),
            status.value(), 
            status.getReasonPhrase(),
            message,
            validationErrors
        ));
    }
   
}
