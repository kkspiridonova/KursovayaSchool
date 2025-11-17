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
import com.example.OnlineSchoolKursach.repository.CommentRepository;
import com.example.OnlineSchoolKursach.model.CommentModel;
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

    @Autowired
    private CommentRepository commentRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
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

            List<SolutionModel> solutions = solutionService.getSolutionsByTaskId(taskId);

            List<CommentModel> comments = commentRepository.findByTaskTaskId(taskId);
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("task", task);
            model.addAttribute("solutions", solutions);
            model.addAttribute("comments", comments);
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
    
    @GetMapping("/teacher/course/{courseId}/create-lesson")
    public String createLessonPage(@PathVariable Long courseId, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {

                if (user.getRole().getRoleName().equals("Студент")) {
                    return "redirect:/student";
                } else if (user.getRole().getRoleName().equals("Преподаватель")) {
                    return "redirect:/teacher";
                } else {
                    return "redirect:/";
                }
            }


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

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {

                if (user.getRole().getRoleName().equals("Студент")) {
                    return "redirect:/student";
                } else if (user.getRole().getRoleName().equals("Преподаватель")) {
                    return "redirect:/teacher";
                } else {
                    return "redirect:/";
                }
            }


            String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
            if (!"Активный".equals(statusName)) {
                redirectAttributes.addFlashAttribute("error", "Уроки можно создавать только когда курс в статусе 'Активный'");
                return "redirect:/teacher/course/" + courseId;
            }
            
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Ошибка в данных урока");
                return "redirect:/teacher/course/" + courseId + "/create-lesson";
            }

            lesson.setCourse(course);

            if (attachedFile != null && !attachedFile.isEmpty()) {
                String filePath = minioFileService.uploadFile(attachedFile, "lesson");
                lesson.setAttachedFile(filePath);
            }

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

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }


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

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }


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

            if (!lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Ошибка: Урок не принадлежит указанному курсу");
                return "redirect:/teacher/course/" + courseId;
            }

            lesson.setCourse(course);

            TaskModel task = new TaskModel();
            task.setLesson(lesson);
            task.setTitle(taskDto.getTitle());
            task.setDescription(taskDto.getDescription());
            task.setDeadline(taskDto.getDeadline());

            if (taskDto.getAttachedFile() != null && !taskDto.getAttachedFile().isEmpty()) {
                String filePath = minioFileService.uploadFile(taskDto.getAttachedFile(), "task");
                task.setAttachedFile(filePath);
            } else if (taskDto.getAttachedFilePath() != null && !taskDto.getAttachedFilePath().isEmpty()) {

                task.setAttachedFile(taskDto.getAttachedFilePath());
            }
            
            System.out.println("Creating task for lesson: " + lesson.getLessonId() + 
                             ", course: " + lesson.getCourse().getCourseId() + 
                             ", title: " + taskDto.getTitle());

            List<TaskStatusModel> statuses = taskService.getAllTaskStatuses();
            if (!statuses.isEmpty()) {
                task.setTaskStatus(statuses.get(0)); // Default to first status
            }

            TaskModel createdTask = taskService.createTask(task);

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

            List<EnrollmentModel> userEnrollments = courseService.getUserEnrollments(user);
            boolean isEnrolled = userEnrollments.stream()
                    .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId));

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

            UserModel user = authService.getUserByEmail(authentication.getName());
            List<EnrollmentModel> enrollments = courseService.getUserEnrollments(user);
            boolean isActiveEnrollment = enrollments.stream()
                    .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId) &&
                            enrollment.getEnrollmentStatus() != null &&
                            "Активен".equals(enrollment.getEnrollmentStatus().getStatusName()));
            
            if (!isActiveEnrollment) {

                return "redirect:/student/course/" + courseId + "?error=Доступ к урокам доступен только для студентов со статусом 'Активен'";
            }

            List<TaskModel> tasks = taskService.getTasksByLessonId(lessonId);

            List<SolutionModel> solutions = new ArrayList<>();
            for (TaskModel task : tasks) {
                try {
                    SolutionModel solution = solutionService.getSolutionByTaskAndUser(task.getTaskId(), user.getUserId());
                    if (solution != null) {
                        solutions.add(solution);
                    }
                } catch (Exception ignored) {

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

            UserModel user = authService.getUserByEmail(authentication.getName());
            List<EnrollmentModel> enrollments = courseService.getUserEnrollments(user);
            boolean isActiveEnrollment = enrollments.stream()
                    .anyMatch(enrollment -> enrollment.getCourse().getCourseId().equals(courseId) &&
                            enrollment.getEnrollmentStatus() != null &&
                            "Активен".equals(enrollment.getEnrollmentStatus().getStatusName()));
            
            if (!isActiveEnrollment) {

                return "redirect:/student/course/" + courseId + "?error=Доступ к заданиям доступен только для студентов со статусом 'Активен'";
            }

            SolutionModel solution = solutionService.getSolutionByTaskAndUser(taskId, user.getUserId());

            List<CommentModel> comments = commentRepository.findByTaskTaskId(taskId);
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("task", task);
            model.addAttribute("solution", solution);
            model.addAttribute("comments", comments);
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

    @GetMapping("/teacher/course/{courseId}/lesson/{lessonId}/edit")
    public String editLessonPage(@PathVariable Long courseId, @PathVariable Long lessonId, 
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

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId;
        }
        
        return "edit-lesson";
    }
    
    @PostMapping("/teacher/course/{courseId}/lesson/{lessonId}/edit")
    public String editLesson(@PathVariable Long courseId, @PathVariable Long lessonId,
                            @RequestParam("title") String title,
                            @RequestParam("content") String content,
                            @RequestParam(value = "attachedFile", required = false) MultipartFile attachedFile,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Урок не найден");
                return "redirect:/teacher/course/" + courseId;
            }

            lesson.setTitle(title);
            lesson.setContent(content);
            LessonModel updatedLesson = lessonService.updateLesson(lessonId, lesson, attachedFile);
            
            if (updatedLesson != null) {
                redirectAttributes.addFlashAttribute("success", "Урок успешно обновлен!");
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId;
            } else {
                redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении урока");
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/edit";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении урока: " + e.getMessage());
            return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/edit";
        }
    }
    
    @PostMapping("/teacher/course/{courseId}/lesson/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            if (lesson == null || !lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Урок не найден");
                return "redirect:/teacher/course/" + courseId;
            }
            
            boolean deleted = lessonService.deleteLesson(lessonId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Урок успешно удален!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Ошибка при удалении урока");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении урока: " + e.getMessage());
        }
        
        return "redirect:/teacher/course/" + courseId;
    }
    
    @GetMapping("/teacher/course/{courseId}/lesson/{lessonId}/task/{taskId}/edit")
    public String editTaskPage(@PathVariable Long courseId, @PathVariable Long lessonId, @PathVariable Long taskId,
                               Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            TaskModel task = taskService.getTaskById(taskId);
            
            if (task == null || !task.getLesson().getLessonId().equals(lessonId) || 
                !lesson.getCourse().getCourseId().equals(courseId)) {
                return "redirect:/teacher/course/" + courseId;
            }

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("task", task);
        } catch (Exception e) {
            return "redirect:/teacher/course/" + courseId;
        }
        
        return "edit-task";
    }
    
    @PostMapping("/teacher/course/{courseId}/lesson/{lessonId}/task/{taskId}/edit")
    public String editTask(@PathVariable Long courseId, @PathVariable Long lessonId, @PathVariable Long taskId,
                          @RequestParam("title") String title,
                          @RequestParam("description") String description,
                          @RequestParam("deadline") String deadlineStr,
                          @RequestParam(value = "attachedFile", required = false) MultipartFile attachedFile,
                          RedirectAttributes redirectAttributes,
                          Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            TaskModel task = taskService.getTaskById(taskId);

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            if (task == null || !task.getLesson().getLessonId().equals(lessonId) || 
                !lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Задание не найдено");
                return "redirect:/teacher/course/" + courseId;
            }

            task.setTitle(title);
            task.setDescription(description);
            task.setDeadline(java.time.LocalDate.parse(deadlineStr));
            TaskModel updatedTask = taskService.updateTask(taskId, task, attachedFile);
            
            if (updatedTask != null) {
                redirectAttributes.addFlashAttribute("success", "Задание успешно обновлено!");
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/task/" + taskId;
            } else {
                redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении задания");
                return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/task/" + taskId + "/edit";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении задания: " + e.getMessage());
            return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId + "/task/" + taskId + "/edit";
        }
    }
    
    @PostMapping("/teacher/course/{courseId}/lesson/{lessonId}/task/{taskId}/delete")
    public String deleteTask(@PathVariable Long courseId, @PathVariable Long lessonId, @PathVariable Long taskId,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);
            LessonModel lesson = lessonService.getLessonById(lessonId);
            TaskModel task = taskService.getTaskById(taskId);

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!course.getTeacher().getUserId().equals(user.getUserId())) {
                return "redirect:/teacher";
            }
            
            if (task == null || !task.getLesson().getLessonId().equals(lessonId) || 
                !lesson.getCourse().getCourseId().equals(courseId)) {
                redirectAttributes.addFlashAttribute("error", "Задание не найдено");
                return "redirect:/teacher/course/" + courseId;
            }
            
            boolean deleted = taskService.deleteTask(taskId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Задание успешно удалено!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Ошибка при удалении задания");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении задания: " + e.getMessage());
        }
        
        return "redirect:/teacher/course/" + courseId + "/lesson/" + lessonId;
    }

    @GetMapping("/teacher/edit-course/{courseId}")
    public String editCoursePage(@PathVariable Long courseId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            CourseModel course = courseService.getCourseById(courseId);

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