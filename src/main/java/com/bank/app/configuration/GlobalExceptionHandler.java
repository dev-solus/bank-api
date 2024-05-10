package com.bank.app.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // Log the exception details
        // e.printStackTrace();

        // Create a response body
        var responseBody = new HashMap<>();
        responseBody.put("message", e.getMessage());
        responseBody.put("error", e.getClass().getSimpleName());
        responseBody.put("code", -HttpStatus.INTERNAL_SERVER_ERROR.value());

        // Add the stack trace
        var stackTrace = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace()) {
            var stackTraceElement = new HashMap<>();
            stackTraceElement.put("file", element.getFileName());
            stackTraceElement.put("line", element.getLineNumber());
            stackTraceElement.put("method", element.getMethodName());
            stackTrace.add(stackTraceElement);
        }
        responseBody.put("stackTrace", stackTrace);

        // Return the response entity
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}