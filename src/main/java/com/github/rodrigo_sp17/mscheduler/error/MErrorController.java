package com.github.rodrigo_sp17.mscheduler.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class will ensure error handling on 404 or 403 gets redirected to main page
 */
@Slf4j
@Controller
@ControllerAdvice
public class MErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest req) {
        log.debug("Redirecting on error");
        Object status = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            if (statusCode == 403 || statusCode == 404) {
                return "redirect:/";
            }
            log.debug(status.toString());
        }
        return "";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleWrongValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        e -> ((FieldError) e).getField(),
                        e -> e.getDefaultMessage() == null ? "" : e.getDefaultMessage(),
                        (previous, recent) -> recent));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        var body = new HashMap<String, String>();
        body.put("error", ex.getReason());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
