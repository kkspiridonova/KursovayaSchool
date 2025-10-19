package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private CourseService courseService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        String error = (String) request.getAttribute("error");
        if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserModel());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserModel user,
                           BindingResult bindingResult,
                           @RequestParam(value = "dataConsent", required = false) boolean dataConsent,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        if (!dataConsent) {
            redirectAttributes.addFlashAttribute("error", "Необходимо дать согласие на обработку персональных данных");
            return "redirect:/register";
        }
        
        try {
            authService.register(user);
            redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminPage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "admin";
    }

    @GetMapping("/teacher")
    public String teacherPage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "teacher";
    }

    @GetMapping("/teacher/edit-course/{courseId}")
    public String editCoursePage(@PathVariable Long courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            return "redirect:/teacher";
        }
        
        return "edit-course";
    }

    @GetMapping("/teacher/course/{courseId}")
    public String teacherCoursePage(@PathVariable Long courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            return "redirect:/teacher";
        }
        
        return "teacher-course";
    }

    @GetMapping("/student")
    public String studentPage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "student";
    }
    
    @GetMapping("/student/course/{courseId}")
    public String studentCoursePage(@PathVariable Long courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            return "redirect:/student";
        }
        
        return "student-course";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            model.addAttribute("user", user);
        } catch (Exception e) {
            return "redirect:/";
        }
        
        return "profile-edit";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/?logout=true";
    }
}