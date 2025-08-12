package com.fintrack.exception;

import com.fintrack.exception.AppExceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- helpers
    private ProblemDetail problem(HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create(type));
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("method", req.getMethod());
        pd.setProperty("instance", req.getRequestURI());
        return pd;
    }

    private ProblemDetail validationProblem(HttpStatus status,
            String type,
            String title,
            String detail,
            Map<String, String> fieldErrors,
            HttpServletRequest req) {
        ProblemDetail pd = problem(status, type, title, detail, req);
        pd.setProperty("errors", fieldErrors);
        return pd;
    }

    // ---- domain
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND,
                "https://fintrack.errors/expense/not-found",
                "Expense not found",
                ex.getMessage(),
                req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST,
                "https://fintrack.errors/common/bad-request",
                "Bad request",
                ex.getMessage(),
                req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN,
                "https://fintrack.errors/auth/forbidden",
                "Forbidden",
                ex.getMessage(),
                req);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT,
                "https://fintrack.errors/common/conflict",
                "Conflict",
                ex.getMessage(),
                req);
    }

    // ---- validation & parsing
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        return validationProblem(HttpStatus.BAD_REQUEST,
                "https://fintrack.errors/validation/invalid-body",
                "Invalid request body",
                "One or more fields are invalid",
                fieldErrors,
                req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> fieldErrors.put(v.getPropertyPath().toString(), v.getMessage()));
        return validationProblem(HttpStatus.BAD_REQUEST,
                "https://fintrack.errors/validation/invalid-params",
                "Invalid request parameters",
                "One or more parameters are invalid",
                fieldErrors,
                req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String detail = String.format("Parameter '%s' expects type '%s' but got '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue());
        return problem(HttpStatus.BAD_REQUEST,
                "https://fintrack.errors/validation/type-mismatch",
                "Parameter type mismatch",
                detail,
                req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String detail = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return problem(HttpStatus.BAD_REQUEST,
                "https://fintrack.errors/validation/missing-param",
                "Missing parameter",
                detail,
                req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST,
                "https://fintrack.errors/validation/malformed-json",
                "Malformed JSON",
                "Request body is invalid or unreadable",
                req);
    }

    // ---- data & security
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT,
                "https://fintrack.errors/db/integrity",
                "Data integrity violation",
                "Operation violates database constraints",
                req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN,
                "https://fintrack.errors/auth/access-denied",
                "Access denied",
                "You do not have permission to perform this action",
                req);
    }

    // ---- wrong path (404 for unknown endpoints)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND,
                "https://fintrack.errors/common/no-handler",
                "Endpoint not found",
                "No endpoint " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                req);
    }

    // ---- fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "https://fintrack.errors/common/unexpected",
                "Unexpected error",
                "Something went wrong. Please contact support if the problem persists.",
                req);
    }
}
