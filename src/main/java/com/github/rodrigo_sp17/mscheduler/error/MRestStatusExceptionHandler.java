package com.github.rodrigo_sp17.mscheduler.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class MRestStatusExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleRestException(ResponseStatusException e) {
        return ResponseEntity
                .status(e.getStatus()).header("error", e.getReason()).build();
    }
}
