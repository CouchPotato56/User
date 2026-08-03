package com.example.userservice.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
   @Override
   protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
         HttpHeaders headers,
         HttpStatusCode status,
         WebRequest request) {
      List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
      ApiErrorResponse errorResponse = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errors,
            request.getDescription(false));
      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      if (ex instanceof ResourceNotFoundException) {
         status = HttpStatus.NOT_FOUND;
      } else if (ex instanceof UnauthorizedException) {
         status = HttpStatus.UNAUTHORIZED;
      }

      ApiErrorResponse errorResponse = new ApiErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            List.of(ex.getMessage()),
            request.getDescription(false));
      return new ResponseEntity<>(errorResponse, status);
   }
}
