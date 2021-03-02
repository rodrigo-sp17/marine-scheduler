package com.github.rodrigo_sp17.mscheduler.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * This class will ensure error handling on 404 or 403 gets redirected to main page
 */
@Controller
public class MErrorController implements ErrorController {

    // TODO - discriminate proper errors
    public final static Logger log = LoggerFactory.getLogger(MErrorController.class.getSimpleName());

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

    @Override
    public String getErrorPath() {
        return null;
    }
}
