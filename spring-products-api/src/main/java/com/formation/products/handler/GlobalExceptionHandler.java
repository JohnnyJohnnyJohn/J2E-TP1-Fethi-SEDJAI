package com.formation.products.handler;

import com.formation.products.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage(), e.getRejectedValue()))
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Validation failed");
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setErrors(errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
            CategoryNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            DuplicateProductException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(409, "Conflict", ex.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CategoryNotEmptyException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotEmpty(
            CategoryNotEmptyException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(409, "Conflict", ex.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(400, "Bad Request", ex.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new FieldError(
                        v.getPropertyPath().toString(),
                        v.getMessage(),
                        v.getInvalidValue()))
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Validation failed");
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setErrors(errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(400, "Bad Request", ex.getMessage());
        error.setPath(request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        ErrorResponse error = new ErrorResponse(
                500, "Internal Server Error", "Une erreur inattendue s'est produite");
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
