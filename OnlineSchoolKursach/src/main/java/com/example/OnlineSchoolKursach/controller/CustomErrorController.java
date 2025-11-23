package com.example.OnlineSchoolKursach.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        String requestPath = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        
        // Игнорируем ошибки для favicon.ico
        if (requestPath != null && requestPath.contains("favicon.ico")) {
            return null; // Возвращаем null, чтобы Spring обработал это как пустой ответ
        }
        
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("status", statusCode);
                model.addAttribute("message", "Страница не найдена");
                return "error/404";
            }
        }
        
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("message", "Что-то пошло не так");
        return "error/500";
    }
}

