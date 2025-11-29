package com.example.securechat.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("details", ex.getBindingResult().getFieldErrors());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({BadRequestException.class, ForbiddenException.class, NotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleCustom(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof ForbiddenException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
