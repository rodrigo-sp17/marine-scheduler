package com.github.rodrigo_sp17.mscheduler;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This class will ensure error handling on 404 or 403 gets redirected to main page
 */
@Controller
public class MErrorController implements ErrorController {

    // TODO - discrimante proper errors

    @RequestMapping("/error")
    public String handleError() {
        return "redirect:/";
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
