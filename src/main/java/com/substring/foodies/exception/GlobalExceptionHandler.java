package com.substring.foodies.exception;

import com.substring.foodies.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Logger logger= LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFound ex)
    {
        ErrorResponse error=ErrorResponse.builder()
                            .message(ex.getMessage())
                            .status(HttpStatus.NO_CONTENT)
                            .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FoodCategoryException.class)
    public ResponseEntity<ErrorResponse> handleFoodCategoryException(FoodCategoryException ex)
    {
        ErrorResponse error=ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex)
    {
        ErrorResponse error=ErrorResponse.builder()
                .message("Invalid Username or Password")
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<String> handleFileNotFoundException(FileNotFoundException ex)
    {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<String> handleInvalidPathException(InvalidPathException ex)
    {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

//    Exception handler in a Spring Boot application that catches validation errors
//    when an incoming request fails @Valid validation.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errorMap = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errorMap.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMap);
    }



    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(DataIntegrityViolationException ex)
    {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST);

        errorResponse.setMessage("Duplicate data detected. One of the provided fields must be unique.");


        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredToken(ExpiredJwtException ex) {

        ex.printStackTrace();
        logger.error("ERROR: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Refresh token is expired. Please login again.");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> handleInvalidToken(MalformedJwtException ex) {

        ex.printStackTrace();
        logger.error("ERROR: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid refresh token format.");
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleInvalidSignature(SignatureException ex) {

        ex.printStackTrace();
        logger.error("ERROR: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid token signature.");
    }

    @ExceptionHandler(io.jsonwebtoken.security.SignatureException.class)
    public ResponseEntity<?> handleJwtSignature(io.jsonwebtoken.security.SignatureException ex){

        ex.printStackTrace();
        logger.error("ERROR: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid JWT Signature");
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex) {

        ex.printStackTrace();
        logger.error("ERROR: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong.");
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<?> internalAuthenticationServiceExceptionHandler(InternalAuthenticationServiceException ex)
    {
        ex.printStackTrace();
        logger.error("ERROR: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message("Username not found.")
                .status(HttpStatus.NOT_FOUND)
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {

        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.FORBIDDEN)
                .build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex)
    {
        ErrorResponse response = ErrorResponse.builder()
                                    .message(ex.getMessage())
                                    .status(HttpStatus.BAD_REQUEST)
                                    .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadItemRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadItemRequest(
            BadItemRequestException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex)
    {
        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
