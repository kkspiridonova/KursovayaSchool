package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.metrics.AppMetricsCollector;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

@ControllerAdvice
public class ErrorHandler {
    @Autowired
    private AppMetricsCollector appMetricsCollector;

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(NoHandlerFoundException ex, Model model) {
        appMetricsCollector.incrementError404Count();
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOther(Exception ex, Model model) {
        return "redirect:/";
    }
}

