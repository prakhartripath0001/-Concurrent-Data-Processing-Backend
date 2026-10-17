package com.Data_Processing_Backend.common;

import com.Data_Processing_Backend.inventory.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(InventoryNotFoundException.class) ResponseEntity<Map<String, String>> missing() { return response(HttpStatus.NOT_FOUND, "Inventory record not found"); }
    @ExceptionHandler({DuplicateInventoryException.class, DataIntegrityViolationException.class}) ResponseEntity<Map<String, String>> duplicate() { return response(HttpStatus.CONFLICT, "Inventory entry code and batch number already exist"); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class}) ResponseEntity<Map<String, String>> invalid(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException validation ? validation.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("Invalid request") : ex.getMessage();
        return response(HttpStatus.BAD_REQUEST, message);
    }
    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) { return ResponseEntity.status(status).body(Map.of("error", message)); }
}
