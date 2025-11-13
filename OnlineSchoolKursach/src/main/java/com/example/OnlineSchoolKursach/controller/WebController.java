package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.LessonModel;
import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.model.EnrollmentModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.dto.TaskDto;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.CourseService;
import com.example.OnlineSchoolKursach.service.LessonService;
import com.example.OnlineSchoolKursach.service.TaskService;
import com.example.OnlineSchoolKursach.service.SolutionService;
import com.example.OnlineSchoolKursach.service.MinioFileService;
import com.example.OnlineSchoolKursach.model.TaskStatusModel;
import com.example.OnlineSchoolKursach.repository.EnrollmentRepository;
import org.springframework.web.multipart.MultipartFile;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SolutionService solutionService;
    
    @Autowired
    private MinioFileService minioFileService;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Exclude attachedFile from model binding to avoid conflict with MultipartFile
        binder.setDisallowedFields("attachedFile");
    }

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
    
    @GetMapping("/teacher/course/{courseId}/lesson/{lessonId}")
    public String teacherLessonPage(@PathVariable Long courseId, @PathVariable Long lessonId, 
                                   Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                return "redirect:/teacher/course/" + courseId;
            }
            
            // Get tasks for this lesson
            List<TaskModel> tasks = taskService.getTasksByLessonId(lessonId);
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("tasks", tasks);
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId;
        }
        
        return "teacher-lesson";
    }
    
    @GetMapping("/teacher/course/{courseId}/lesson/{lessonId}/task/{taskId}")
    public String teacherTaskPage(@PathVariable Long courseId, @PathVariable Long lessonId, 
                                 @PathVariable Long taskId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            TaskModel task = taskService.getTaskById(taskId);
            
            if (task == null || !task.getLesson().getLessonId().equals(lessonId)) {
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId;
            }
            
            // Get solutions for this task
            List<SolutionModel> solutions = solutionService.getSolutionsByTaskId(taskId);
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("task", task);
            model.addAttribute("solutions", solutions);
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId;
        }
        
        return "teacher-task";
    }

    @GetMapping("/teacher/course/{courseId}/materials")
    public String teacherCourseMaterialsPage(@PathVariable Long courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            CourseModel course = courseService.getCourseById(courseId);
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }

            // Try to load lessons for the course and gather materials
            List<LessonModel> lessons = lessonService.getLessonsByCourseId(courseId);
            List<TaskModel> allTasks = new ArrayList<>();
            for (LessonModel lesson : lessons) {
                List<TaskModel> tasks = taskService.getTasksByLessonId(lesson.getLessonId());
                if (tasks != null && !tasks.isEmpty()) {
                    allTasks.addAll(tasks);
                }
            }

            model.addAttribute("course", course);
            model.addAttribute("lessons", lessons);
            model.addAttribute("tasks", allTasks);
            return "teacher-course-materials";
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId;
        }
    }
    
    @PostMapping("/teacher/course/{courseId}/lesson/{lessonId}/task/{taskId}/delete")
    public String deleteTask(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             @PathVariable Long taskId,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            TaskModel task = taskService.getTaskById(taskId);
            
            // Validate ownership and associations
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                return "redirect:/teacher/course/" + courseId;
            }
            if (task == null || !task.getLesson().getLessonId().equals(lessonId)) {
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId;
            }
            
            // Delete task (service also handles attached files cleanup)
            taskService.deleteTask(taskId);
            redirectAttributes.addFlashAttribute("success", "Задание удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении задания: " + e.getMessage());
        }
        
        return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId;
    }
    
    @GetMapping("/teacher/course/{courseId}/create-lesson")
    public String createLessonPage(@PathVariable Long courseId, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            
            // Check if user is the teacher of this course
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                // Redirect based on user role
                if (user.getRole().getRoleName().equals("Студент")) {
                    return "redirect:/student";
                } else if (user.getRole().getRoleName().equals("Преподаватель")) {
                    return "redirect:/teacher";
                } else {
                    return "redirect:/";
                }
            }

            // Restrict creating lessons when course is recruiting or filled
            // Can only create lessons when course is Active
            String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
            System.out.println("Course status when creating lesson: " + statusName);
            System.out.println("Course start date: " + course.getStartDate());
            System.out.println("Today: " + java.time.LocalDate.now());
            if (!"Активный".equals(statusName)) {
                redirectAttributes.addFlashAttribute("error", "Уроки можно создавать только когда курс в статусе 'Активный'. Текущий статус: " + (statusName != null ? statusName : "не установлен"));
                return "redirect:/teacher/course/" + courseId;
            }

            model.addAttribute("course", course);
            model.addAttribute("lesson", new LessonModel());
            return "create-lesson";
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId;
        }
    }
    
    @PostMapping("/teacher/course/{courseId}/create-lesson")
    public String createLesson(@PathVariable Long courseId, 
                              @Valid @ModelAttribute("lesson") LessonModel lesson,
                              @RequestParam(value = "attachedFile", required = false) MultipartFile attachedFile,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            
            // Check if user is the teacher of this course
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                // Redirect based on user role
                if (user.getRole().getRoleName().equals("Студент")) {
                    return "redirect:/student";
                } else if (user.getRole().getRoleName().equals("Преподаватель")) {
                    return "redirect:/teacher";
                } else {
                    return "redirect:/";
                }
            }
            
            // Restrict creating lessons when course is recruiting or filled
            // Can only create lessons when course is Active
            String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
            if (!"Активный".equals(statusName)) {
                redirectAttributes.addFlashAttribute("error", "Уроки можно создавать только когда курс в статусе 'Активный'");
                return "redirect:/teacher/course/" + courseId;
            }
            
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Ошибка в данных урока");
                return "redirect:/teacher/course/" + courseId + "/create-lesson";
            }
            
            // Set the course for this lesson
            lesson.setCourse(course);
            
            // Upload attached file if provided
            if (attachedFile != null && !attachedFile.isEmpty()) {
                String filePath = minioFileService.uploadFile(attachedFile, "lesson");
                lesson.setAttachedFile(filePath);
            }
            
            // Create the lesson
            LessonModel createdLesson = lessonService.createLesson(lesson);
            
            redirectAttributes.addFlashAttribute("success", "Урок успешно создан!");
            return "redirect:/teacher/course/" + courseId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании урока: " + e.getMessage());
            return "redirect:/teacher/course/" + courseId + "/create-lesson";
        }
    }
    
    @GetMapping("/teacher/course/{courseId}/lesson/{lessonId}/create-task")
    public String createTaskPage(@PathVariable Long courseId, @PathVariable Long lessonId, 
                                Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            
            // Check if user is the teacher of this course
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }

            // Restrict creating tasks when course is recruiting or filled
            // Can only create tasks when course is Active
            String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
            System.out.println("Course status when creating task: " + statusName);
            System.out.println("Course start date: " + course.getStartDate());
            System.out.println("Today: " + java.time.LocalDate.now());
            if (!"Активный".equals(statusName)) {
                redirectAttributes.addFlashAttribute("error", "Задания можно создавать только когда курс в статусе 'Активный'. Текущий статус: " + (statusName != null ? statusName : "не установлен"));
                return "redirect:/teacher/course/" + courseId;
            }
            
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                return "redirect:/teacher/course/" + courseId;
            }
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("taskDto", new TaskDto());
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId;
        }
        
        return "create-task";
    }
    
    @PostMapping("/teacher/course/{courseId}/lesson/{lessonId}/create-task")
    public String createTask(@PathVariable Long courseId, @PathVariable Long lessonId,
                            @Valid @ModelAttribute("taskDto") TaskDto taskDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            
            // Check if user is the teacher of this course
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            // Restrict creating tasks when course is recruiting or filled
            // Can only create tasks when course is Active
            String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
            if (!"Активный".equals(statusName)) {
                redirectAttributes.addFlashAttribute("error", "Задания можно создавать только когда курс в статусе 'Активный'");
                return "redirect:/teacher/course/" + courseId;
            }
            
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Ошибка: Неверный урок или курс");
                return "redirect:/teacher/course/" + courseId;
            }
            
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Ошибка в данных задания");
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/create-task";
            }
            
            // Verify lesson belongs to the correct course
            if (!lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Ошибка: Урок не принадлежит указанному курсу");
                return "redirect:/teacher/course/" + courseId;
            }
            
            // Force-link lesson to the exact course from path to avoid any mismatch
            lesson.setCourse(course);

            // Convert DTO to TaskModel
            TaskModel task = new TaskModel();
            task.setLesson(lesson);
            task.setTitle(taskDto.getTitle());
            task.setDescription(taskDto.getDescription());
            task.setDeadline(taskDto.getDeadline());
            
            // Upload attached file if provided
            if (taskDto.getAttachedFile() != null && !taskDto.getAttachedFile().isEmpty()) {
                String filePath = minioFileService.uploadFile(taskDto.getAttachedFile(), "task");
                task.setAttachedFile(filePath);
            } else if (taskDto.getAttachedFilePath() != null && !taskDto.getAttachedFilePath().isEmpty()) {
                // Fallback to path if file not uploaded
                task.setAttachedFile(taskDto.getAttachedFilePath());
            }
            
            System.out.println("Creating task for lesson: " + lesson.getLessonId() + 
                             ", course: " + lesson.getCourse().getCourseId() + 
                             ", title: " + taskDto.getTitle());
            
            // Set default status
            List<TaskStatusModel> statuses = taskService.getAllTaskStatuses();
            if (!statuses.isEmpty()) {
                task.setTaskStatus(statuses.get(0)); // Default to first status
            }
            
            // Create the task
            TaskModel createdTask = taskService.createTask(task);
            
            // Use IDs from persisted entities to avoid any mismatch
            Long redirectCourseId = createdTask.getLesson().getCourse().getCourseId();
            Long redirectLessonId = createdTask.getLesson().getLessonId();
            
            redirectAttributes.addFlashAttribute("success", "Задание успешно создано!");
            return "redirect:/teacher/course/" + redirectCourseId + "/lesson/" + redirectLessonId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании задания: " + e.getMessage());
            return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/create-task";
        }
    }
    
    @GetMapping("/teacher/solution/{solutionId}")
    public String teacherSolutionPage(@PathVariable Long solutionId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            SolutionModel solution = solutionService.getSolutionById(solutionId);
            
            // Check if user is the teacher of this course
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!solution.getTask().getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            model.addAttribute("solution", solution);
            model.addAttribute("course", solution.getTask().getLesson().getCourse());
            model.addAttribute("lesson", solution.getTask().getLesson());
            model.addAttribute("task", solution.getTask());
            model.addAttribute("student", solution.getUser());
        } catch (Exception e) {
            return "redirect:/teacher";
        }
        
        return "teacher-solution";
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
            UserModel user = authService.getUserByEmail(authentication.getName());
            
            // Check if user is enrolled in this course
            List<EnrollmentModel> userEnrollments = courseService.getUserEnrollments(user);
            boolean isEnrolled = userEnrollments.stream()
                    .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId));
            
            // Get enrolled students count
            List<EnrollmentModel> courseEnrollments = enrollmentRepository.findByCourseCourseId(courseId);
            int enrolledCount = courseEnrollments.size();
            
            System.out.println("User: " + user.getEmail() + " enrolled in course " + courseId + ": " + isEnrolled);
            System.out.println("User enrollments: " + userEnrollments.size());
            
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
            model.addAttribute("isEnrolled", isEnrolled);
            model.addAttribute("enrolledCount", enrolledCount);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/student";
        }
        
        return "student-course";
    }
    
    @GetMapping("/student/course/{courseId}/lesson/{lessonId}")
    public String studentLessonPage(@PathVariable Long courseId, @PathVariable Long lessonId, 
                                   Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                return "redirect:/student/course/" + courseId;
            }
            
            // Проверяем, что студент зачислен и имеет статус "Активен"
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<EnrollmentModel> enrollments = courseService.getUserEnrollments(user);
            boolean isActiveEnrollment = enrollments.stream()
                    .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId) &&
                            enrollment.getEnrollmentStatus() != null &&
                            "Активен".equals(enrollment.getEnrollmentStatus().getStatusName()));
            
            if (!isActiveEnrollment) {
                // Если студент не зачислен или имеет статус "Ожидает подтверждения", редиректим
                return "redirect:/student/course/" + courseId + "?error=Доступ к урокам доступен только для студентов со статусом 'Активен'";
            }
            
            // Get tasks for this lesson
            List<TaskModel> tasks = taskService.getTasksByLessonId(lessonId);
            
            // Get user solutions for these tasks
            List<SolutionModel> solutions = new ArrayList<>();
            for (TaskModel task : tasks) {
                try {
                    SolutionModel solution = solutionService.getSolutionByTaskAndUser(task.getTaskId(), user.getUserId());
                    if (solution != null) {
                        solutions.add(solution);
                    }
                } catch (Exception ignored) {
                    // If any duplicate/loader issue occurs, skip this task's solution to prevent redirect
                }
            }
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("tasks", tasks);
            model.addAttribute("solutions", solutions);
        } catch (Exception e) {
            return "redirect:/student/course/" + courseId;
        }
        
        return "student-lesson";
    }
    
    @GetMapping("/student/course/{courseId}/lesson/{lessonId}/task/{taskId}")
    public String studentTaskPage(@PathVariable Long courseId, @PathVariable Long lessonId, 
                                 @PathVariable Long taskId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            TaskModel task = taskService.getTaskById(taskId);
            
            if (task == null || !task.getLesson().getLessonId().equals(lessonId)) {
                return "redirect:/student/course/" + courseId + "/lesson/" + lessonId;
            }
            
            // Проверяем, что студент зачислен и имеет статус "Активен"
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<EnrollmentModel> enrollments = courseService.getUserEnrollments(user);
            boolean isActiveEnrollment = enrollments.stream()
                    .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId) &&
                            enrollment.getEnrollmentStatus() != null &&
                            "Активен".equals(enrollment.getEnrollmentStatus().getStatusName()));
            
            if (!isActiveEnrollment) {
                // Если студент не зачислен или имеет статус "Ожидает подтверждения", редиректим
                return "redirect:/student/course/" + courseId + "?error=Доступ к заданиям доступен только для студентов со статусом 'Активен'";
            }
            
            // Get user solution for this task
            SolutionModel solution = solutionService.getSolutionByTaskAndUser(taskId, user.getUserId());
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("task", task);
            model.addAttribute("solution", solution);
        } catch (Exception e) {
            return "redirect:/student/course/" + courseId + "/lesson/" + lessonId;
        }
        
        return "student-task";
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

    @GetMapping("/teacher/edit-course/{courseId}")
    public String editCoursePage(@PathVariable Long courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            
            // Check if user is the teacher of this course
            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            model.addAttribute("course", course);
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            return "redirect:/teacher";
        }
        
        return "edit-course";
    }

    @GetMapping("/gift-cards")
    public String giftCardsPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "gift-cards";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/?logout=true";
    }
}