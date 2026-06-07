package com.abdoul.gentech_fintech.Exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionHandling {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, String>> handleValidationException (MethodArgumentNotValidException ex){
        Map <String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    private ResponseEntity <Map<String, String>> handleRuntimeException (RuntimeException ex){
        Map <String, String> errors = new LinkedHashMap<>();

        ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = (annotation != null)? annotation.value(): HttpStatus.INTERNAL_SERVER_ERROR;

        errors.put("error", ex.getMessage());

        return ResponseEntity.status(status).body(errors);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity <Map<String, String>> handleGlobalException (Exception ex){
        log.error("error: {}, cause: {}", ex.getMessage(), ex.getCause());

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("error", "Something went wrong. Please try again");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<Map<String, String>> handleIoException (IOException ex){
        Map<String, String> errors = new LinkedHashMap<>();

        errors.put("error", "File upload failed");

        log.error("file error {}", ex.getCause(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }
}
