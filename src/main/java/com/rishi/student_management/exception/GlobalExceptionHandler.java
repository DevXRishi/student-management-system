package com.rishi.student_management.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import com.rishi.student_management.payload.ErrorResponse;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class);


    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudentNotFound(
            StudentNotFoundException ex,
            HttpServletRequest request) {


        ErrorResponse error = new ErrorResponse();

        error.setTimestamp(LocalDateTime.now());

        error.setStatus(HttpStatus.NOT_FOUND.value());

        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());

        error.setMessage(ex.getMessage());

        error.setPath(request.getRequestURI());

        logger.error(
                "Student Not Found : {}",
                ex.getMessage());

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->

                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )

                );

        ErrorResponse response = new ErrorResponse();

        response.setTimestamp(LocalDateTime.now());

        response.setStatus(HttpStatus.BAD_REQUEST.value());

        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        response.setMessage("Validation Failed");

        response.setPath(request.getRequestURI());

        response.setValidationErrors(validationErrors);

        logger.error("Validation Failed : {}", validationErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse();

        error.setTimestamp(LocalDateTime.now());

        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());

        error.setMessage("Something went wrong. Please try again later.");

        error.setPath(request.getRequestURI());

        logger.error("Unexpected Exception : ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

}