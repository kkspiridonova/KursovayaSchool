package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.CourseService;
import com.example.OnlineSchoolKursach.service.CertificateScheduler;
import com.example.OnlineSchoolKursach.service.StatisticsService;
import com.example.OnlineSchoolKursach.service.ExportImportService;
import com.example.OnlineSchoolKursach.dto.CourseStatisticsDto;
import com.example.OnlineSchoolKursach.dto.AuditLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/admin")
@Tag(name = "Admin Management", description = "API для административных функций")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentStatusRepository enrollmentStatusRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CertificateScheduler certificateScheduler;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private GiftCardRepository giftCardRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseStatusRepository courseStatusRepository;

    @Autowired
    private LessonStatusRepository lessonStatusRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private SolutionStatusRepository solutionStatusRepository;

    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    @Autowired
    private GiftCardStatusRepository giftCardStatusRepository;

    @Autowired
    private CertificateStatusRepository certificateStatusRepository;

    @Autowired
    private ExportImportService exportImportService;

    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей", description = "Получение списка всех пользователей системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = UserModel.class)))
    })
    public ResponseEntity<List<UserModel>> getAllUsers(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<UserModel> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<UserModel> getUserById(@PathVariable Long id, Authentication authentication) {
        try {
            UserModel user = userRepository.findById(id).orElse(null);
            if (user == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Получить все категории", description = "Получение списка всех категорий курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CategoryModel.class)))
    })
    public ResponseEntity<List<CategoryModel>> getAllCategories(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<CategoryModel> categories = categoryRepository.findAll();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Получить категорию по ID")
    public ResponseEntity<CategoryModel> getCategoryById(@PathVariable Long id, Authentication authentication) {
        try {
            CategoryModel category = categoryRepository.findById(id).orElse(null);
            if (category == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/enrollments")
    @Operation(summary = "Получить все записи на курсы", description = "Получение списка всех записей пользователей на курсы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список записей успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentModel.class)))
    })
    public ResponseEntity<List<EnrollmentModel>> getAllEnrollments(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<EnrollmentModel> enrollments = enrollmentRepository.findAll();
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/enrollments/{id}")
    @Operation(summary = "Получить запись на курс по ID")
    public ResponseEntity<EnrollmentModel> getEnrollmentById(@PathVariable Long id, Authentication authentication) {
        try {
            EnrollmentModel enrollment = enrollmentRepository.findById(id).orElse(null);
            if (enrollment == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(enrollment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/enrollment-statuses")
    @Operation(summary = "Получить все статусы записей", description = "Получение списка всех статусов записей на курсы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список статусов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentStatusModel.class)))
    })
    public ResponseEntity<List<EnrollmentStatusModel>> getAllEnrollmentStatuses(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<EnrollmentStatusModel> statuses = enrollmentStatusRepository.findAll();
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/enrollments/{enrollmentId}/status")
    @Operation(summary = "Обновить статус записи", description = "Обновление статуса записи на курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус записи успешно обновлен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentModel.class))),
            @ApiResponse(responseCode = "404", description = "Запись или статус не найдены")
    })
    public ResponseEntity<EnrollmentModel> updateEnrollmentStatus(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication,
            @Parameter(description = "ID записи") 
            @PathVariable Long enrollmentId,
            @Parameter(description = "ID нового статуса") 
            @RequestParam Long statusId) {
        try {
            EnrollmentModel enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
            if (enrollment == null) {
                return ResponseEntity.notFound().build();
            }

            EnrollmentStatusModel status = enrollmentStatusRepository.findById(statusId).orElse(null);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }

            enrollment.setEnrollmentStatus(status);
            EnrollmentModel updatedEnrollment = enrollmentRepository.save(enrollment);

            if (updatedEnrollment.getCourse() != null) {
                courseService.checkAndUpdateCourseStatusIfFull(updatedEnrollment.getCourse());
            }

            return ResponseEntity.ok(updatedEnrollment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/files")
    @Operation(summary = "Получить все файлы", description = "Получение списка всех файлов в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список файлов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = FileModel.class)))
    })
    public ResponseEntity<List<FileModel>> getAllFiles(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            List<FileModel> files = fileRepository.findAll();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/certificates/check-and-issue")
    @Operation(summary = "Ручной запуск проверки и выдачи сертификатов", 
               description = "Запускает проверку завершенных курсов и выдает сертификаты студентам (для тестирования)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проверка выполнена успешно"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    })
    public ResponseEntity<Map<String, String>> checkAndIssueCertificatesManually(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, String> response = new HashMap<>();

        boolean isAuthenticated = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isAuthenticated = true;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            isAuthenticated = true;
        }
        
        if (!isAuthenticated) {
            response.put("error", "Не авторизован. Пожалуйста, войдите в систему.");
            response.put("status", "error");
            return ResponseEntity.status(401).body(response);
        }

        try {
            certificateScheduler.checkAndIssueCertificates();
            response.put("message", "Проверка сертификатов выполнена. Проверьте логи для деталей.");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking and issuing certificates: {}", e.getMessage(), e);
            response.put("error", "Ошибка при проверке сертификатов: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/statistics/courses")
    @Operation(summary = "Получить статистику по всем курсам", description = "Получение статистики по всем курсам из VIEW")
    public ResponseEntity<List<CourseStatisticsDto>> getAllCoursesStatistics(Authentication authentication) {
        try {
            List<CourseStatisticsDto> stats = statisticsService.getAllCoursesStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting courses statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics/courses/{courseId}")
    @Operation(summary = "Получить статистику по курсу", description = "Получение детальной статистики по конкретному курсу")
    public ResponseEntity<CourseStatisticsDto> getCourseStatistics(
            @PathVariable Long courseId, 
            Authentication authentication) {
        try {
            CourseStatisticsDto stats = statisticsService.getCourseStatistics(courseId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting course statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics/dashboard")
    @Operation(summary = "Получить статистику для дашборда", description = "Получение общей статистики для главной панели")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Authentication authentication) {
        try {
            Map<String, Object> stats = statisticsService.getDashboardStats(dateFrom, dateTo);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting dashboard stats: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audit/logs")
    @Operation(summary = "Получить логи аудита", description = "Получение последних логов аудита")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs(
            @RequestParam(defaultValue = "100") int limit,
            Authentication authentication) {
        try {
            List<AuditLogDto> logs = statisticsService.getAllRecentHistory(limit);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error getting audit logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audit/courses/{courseId}")
    @Operation(summary = "Получить историю изменений курса", description = "Получение истории всех изменений конкретного курса")
    public ResponseEntity<List<AuditLogDto>> getCourseAudit(
            @PathVariable Long courseId,
            Authentication authentication) {
        try {
            List<AuditLogDto> logs = statisticsService.getCourseHistory(courseId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error getting course audit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/audit/users/{userId}")
    @Operation(summary = "Получить историю действий пользователя", description = "Получение истории действий конкретного пользователя")
    public ResponseEntity<List<AuditLogDto>> getUserAudit(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {
        try {
            List<AuditLogDto> logs = statisticsService.getUserHistory(userId, limit);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error getting user audit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }




    @PostMapping("/users")
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<UserModel> createUser(@RequestBody UserModel user, Authentication authentication) {
        try {
            user.setRegistrationDate(java.time.LocalDate.now());
            UserModel saved = userRepository.save(user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Обновить пользователя (только роль)")
    public ResponseEntity<UserModel> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData, Authentication authentication) {
        try {
            UserModel existing = userRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();

            if (userData.containsKey("role")) {
                Map<String, Object> roleData = (Map<String, Object>) userData.get("role");
                if (roleData.containsKey("roleId")) {
                    Long roleId = Long.valueOf(roleData.get("roleId").toString());
                    RoleModel role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        existing.setRole(role);
                        UserModel updated = userRepository.save(existing);
                        return ResponseEntity.ok(updated);
                    }
                }
            }
            
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/courses")
    @Operation(summary = "Получить все курсы")
    public ResponseEntity<List<CourseModel>> getAllCourses(Authentication authentication) {
        try {
            List<CourseModel> courses = courseRepository.findAll();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Получить курс по ID")
    public ResponseEntity<CourseModel> getCourseById(@PathVariable Long id, Authentication authentication) {
        try {
            CourseModel course = courseRepository.findById(id).orElse(null);
            if (course == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/courses")
    @Operation(summary = "Создать курс")
    public ResponseEntity<CourseModel> createCourse(@RequestBody CourseModel course, Authentication authentication) {
        try {
            CourseModel saved = courseRepository.save(course);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating course: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/courses/{id}")
    @Operation(summary = "Обновить курс")
    public ResponseEntity<CourseModel> updateCourse(@PathVariable Long id, @RequestBody CourseModel course, Authentication authentication) {
        try {
            CourseModel existing = courseRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            course.setCourseId(id);
            CourseModel updated = courseRepository.save(course);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating course: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/courses/{id}")
    @Operation(summary = "Удалить курс")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id, Authentication authentication) {
        try {
            if (!courseRepository.existsById(id)) return ResponseEntity.notFound().build();
            courseRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting course: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/categories")
    @Operation(summary = "Создать категорию")
    public ResponseEntity<CategoryModel> createCategory(@RequestBody CategoryModel category, Authentication authentication) {
        try {
            CategoryModel saved = categoryRepository.save(category);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Обновить категорию")
    public ResponseEntity<CategoryModel> updateCategory(@PathVariable Long id, @RequestBody CategoryModel category, Authentication authentication) {
        try {
            CategoryModel existing = categoryRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            category.setCategoryId(id);
            CategoryModel updated = categoryRepository.save(category);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Удалить категорию")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Authentication authentication) {
        try {
            if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build();
            categoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/enrollments")
    @Operation(summary = "Создать запись на курс")
    public ResponseEntity<EnrollmentModel> createEnrollment(@RequestBody EnrollmentModel enrollment, Authentication authentication) {
        try {
            if (enrollment.getEnrollmentDate() == null) enrollment.setEnrollmentDate(java.time.LocalDate.now());
            EnrollmentModel saved = enrollmentRepository.save(enrollment);
            
            if (saved.getCourse() != null) {
                courseService.checkAndUpdateCourseStatusIfFull(saved.getCourse());
            }
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating enrollment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/enrollments/{id}")
    @Operation(summary = "Обновить запись на курс")
    public ResponseEntity<EnrollmentModel> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentModel enrollment, Authentication authentication) {
        try {
            EnrollmentModel existing = enrollmentRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            enrollment.setEnrollmentId(id);
            EnrollmentModel updated = enrollmentRepository.save(enrollment);
            
            if (updated.getCourse() != null) {
                courseService.checkAndUpdateCourseStatusIfFull(updated.getCourse());
            }
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating enrollment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/enrollments/{id}")
    @Operation(summary = "Удалить запись на курс")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id, Authentication authentication) {
        try {
            if (!enrollmentRepository.existsById(id)) return ResponseEntity.notFound().build();
            enrollmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting enrollment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/lessons")
    @Operation(summary = "Получить все уроки")
    public ResponseEntity<List<LessonModel>> getAllLessons(Authentication authentication) {
        try {
            List<LessonModel> lessons = lessonRepository.findAll();
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/lessons/{id}")
    @Operation(summary = "Получить урок по ID")
    public ResponseEntity<LessonModel> getLessonById(@PathVariable Long id, Authentication authentication) {
        try {
            LessonModel lesson = lessonRepository.findById(id).orElse(null);
            if (lesson == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(lesson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/lessons")
    @Operation(summary = "Создать урок")
    public ResponseEntity<LessonModel> createLesson(@RequestBody LessonModel lesson, Authentication authentication) {
        try {
            LessonModel saved = lessonRepository.save(lesson);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating lesson: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/lessons/{id}")
    @Operation(summary = "Обновить урок")
    public ResponseEntity<LessonModel> updateLesson(@PathVariable Long id, @RequestBody LessonModel lesson, Authentication authentication) {
        try {
            LessonModel existing = lessonRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            lesson.setLessonId(id);
            LessonModel updated = lessonRepository.save(lesson);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating lesson: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/lessons/{id}")
    @Operation(summary = "Удалить урок")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id, Authentication authentication) {
        try {
            if (!lessonRepository.existsById(id)) return ResponseEntity.notFound().build();
            lessonRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting lesson: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tasks")
    @Operation(summary = "Получить все задания")
    public ResponseEntity<List<TaskModel>> getAllTasks(Authentication authentication) {
        try {
            List<TaskModel> tasks = taskRepository.findAll();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Получить задание по ID")
    public ResponseEntity<TaskModel> getTaskById(@PathVariable Long id, Authentication authentication) {
        try {
            TaskModel task = taskRepository.findById(id).orElse(null);
            if (task == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/tasks")
    @Operation(summary = "Создать задание")
    public ResponseEntity<TaskModel> createTask(@RequestBody TaskModel task, Authentication authentication) {
        try {
            TaskModel saved = taskRepository.save(task);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Обновить задание")
    public ResponseEntity<TaskModel> updateTask(@PathVariable Long id, @RequestBody TaskModel task, Authentication authentication) {
        try {
            TaskModel existing = taskRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            task.setTaskId(id);
            TaskModel updated = taskRepository.save(task);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating task: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Удалить задание")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        try {
            if (!taskRepository.existsById(id)) return ResponseEntity.notFound().build();
            taskRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting task: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/solutions")
    @Operation(summary = "Получить все решения")
    public ResponseEntity<List<SolutionModel>> getAllSolutions(Authentication authentication) {
        try {
            List<SolutionModel> solutions = solutionRepository.findAll();
            return ResponseEntity.ok(solutions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/solutions/{id}")
    @Operation(summary = "Получить решение по ID")
    public ResponseEntity<SolutionModel> getSolutionById(@PathVariable Long id, Authentication authentication) {
        try {
            SolutionModel solution = solutionRepository.findById(id).orElse(null);
            if (solution == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(solution);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/solutions")
    @Operation(summary = "Создать решение")
    public ResponseEntity<SolutionModel> createSolution(@RequestBody SolutionModel solution, Authentication authentication) {
        try {
            if (solution.getSubmitDate() == null) solution.setSubmitDate(java.time.LocalDate.now());
            SolutionModel saved = solutionRepository.save(solution);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating solution: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/solutions/{id}")
    @Operation(summary = "Обновить решение")
    public ResponseEntity<SolutionModel> updateSolution(@PathVariable Long id, @RequestBody SolutionModel solution, Authentication authentication) {
        try {
            SolutionModel existing = solutionRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            solution.setSolutionId(id);
            SolutionModel updated = solutionRepository.save(solution);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating solution: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/solutions/{id}")
    @Operation(summary = "Удалить решение")
    public ResponseEntity<Void> deleteSolution(@PathVariable Long id, Authentication authentication) {
        try {
            if (!solutionRepository.existsById(id)) return ResponseEntity.notFound().build();
            solutionRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting solution: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/grades")
    @Operation(summary = "Получить все оценки")
    public ResponseEntity<List<GradeModel>> getAllGrades(Authentication authentication) {
        try {
            List<GradeModel> grades = gradeRepository.findAll();
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/grades/{id}")
    @Operation(summary = "Получить оценку по ID")
    public ResponseEntity<GradeModel> getGradeById(@PathVariable Long id, Authentication authentication) {
        try {
            GradeModel grade = gradeRepository.findById(id).orElse(null);
            if (grade == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(grade);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/grades")
    @Operation(summary = "Создать оценку")
    public ResponseEntity<GradeModel> createGrade(@RequestBody GradeModel grade, Authentication authentication) {
        try {
            GradeModel saved = gradeRepository.save(grade);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating grade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/grades/{id}")
    @Operation(summary = "Обновить оценку")
    public ResponseEntity<GradeModel> updateGrade(@PathVariable Long id, @RequestBody GradeModel grade, Authentication authentication) {
        try {
            GradeModel existing = gradeRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            grade.setGradeId(id);
            GradeModel updated = gradeRepository.save(grade);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating grade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/grades/{id}")
    @Operation(summary = "Удалить оценку")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id, Authentication authentication) {
        try {
            if (!gradeRepository.existsById(id)) return ResponseEntity.notFound().build();
            gradeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting grade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/certificates")
    @Operation(summary = "Получить все сертификаты")
    public ResponseEntity<List<CertificateModel>> getAllCertificates(Authentication authentication) {
        try {
            List<CertificateModel> certificates = certificateRepository.findAll();
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/certificates/{id}")
    @Operation(summary = "Получить сертификат по ID")
    public ResponseEntity<CertificateModel> getCertificateById(@PathVariable Long id, Authentication authentication) {
        try {
            CertificateModel certificate = certificateRepository.findById(id).orElse(null);
            if (certificate == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/certificates")
    @Operation(summary = "Создать сертификат")
    public ResponseEntity<CertificateModel> createCertificate(@RequestBody CertificateModel certificate, Authentication authentication) {
        try {
            if (certificate.getIssueDate() == null) certificate.setIssueDate(java.time.LocalDate.now());
            if (certificate.getEmailSent() == null) certificate.setEmailSent(false);
            CertificateModel saved = certificateRepository.save(certificate);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating certificate: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/certificates/{id}")
    @Operation(summary = "Обновить сертификат")
    public ResponseEntity<CertificateModel> updateCertificate(@PathVariable Long id, @RequestBody CertificateModel certificate, Authentication authentication) {
        try {
            CertificateModel existing = certificateRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            certificate.setCertificateId(id);
            CertificateModel updated = certificateRepository.save(certificate);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating certificate: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/certificates/{id}")
    @Operation(summary = "Удалить сертификат")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id, Authentication authentication) {
        try {
            if (!certificateRepository.existsById(id)) return ResponseEntity.notFound().build();
            certificateRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting certificate: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/checks")
    @Operation(summary = "Получить все чеки")
    public ResponseEntity<List<CheckModel>> getAllChecks(Authentication authentication) {
        try {
            List<CheckModel> checks = checkRepository.findAll();
            return ResponseEntity.ok(checks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/checks/{id}")
    @Operation(summary = "Получить чек по ID")
    public ResponseEntity<CheckModel> getCheckById(@PathVariable Long id, Authentication authentication) {
        try {
            CheckModel check = checkRepository.findById(id).orElse(null);
            if (check == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(check);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/checks")
    @Operation(summary = "Создать чек")
    public ResponseEntity<CheckModel> createCheck(@RequestBody CheckModel check, Authentication authentication) {
        try {
            if (check.getPaymentDate() == null) check.setPaymentDate(java.time.LocalDate.now());
            CheckModel saved = checkRepository.save(check);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating check: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/checks/{id}")
    @Operation(summary = "Обновить чек")
    public ResponseEntity<CheckModel> updateCheck(@PathVariable Long id, @RequestBody CheckModel check, Authentication authentication) {
        try {
            CheckModel existing = checkRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            check.setCheckId(id);
            CheckModel updated = checkRepository.save(check);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating check: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/checks/{id}")
    @Operation(summary = "Удалить чек")
    public ResponseEntity<Void> deleteCheck(@PathVariable Long id, Authentication authentication) {
        try {
            if (!checkRepository.existsById(id)) return ResponseEntity.notFound().build();
            checkRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting check: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/gift-cards")
    @Operation(summary = "Получить все подарочные карты")
    public ResponseEntity<List<GiftCardModel>> getAllGiftCards(Authentication authentication) {
        try {
            List<GiftCardModel> giftCards = giftCardRepository.findAll();
            return ResponseEntity.ok(giftCards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/gift-cards/{id}")
    @Operation(summary = "Получить подарочную карту по ID")
    public ResponseEntity<GiftCardModel> getGiftCardById(@PathVariable Long id, Authentication authentication) {
        try {
            GiftCardModel giftCard = giftCardRepository.findById(id).orElse(null);
            if (giftCard == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(giftCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/gift-cards")
    @Operation(summary = "Создать подарочную карту")
    public ResponseEntity<GiftCardModel> createGiftCard(@RequestBody GiftCardModel giftCard, Authentication authentication) {
        try {
            if (giftCard.getIssueDate() == null) giftCard.setIssueDate(java.time.LocalDate.now());
            if (giftCard.getBalance() == null) giftCard.setBalance(java.math.BigDecimal.ZERO);
            GiftCardModel saved = giftCardRepository.save(giftCard);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating gift card: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/gift-cards/{id}")
    @Operation(summary = "Обновить подарочную карту")
    public ResponseEntity<GiftCardModel> updateGiftCard(@PathVariable Long id, @RequestBody GiftCardModel giftCard, Authentication authentication) {
        try {
            GiftCardModel existing = giftCardRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            giftCard.setGiftCardId(id);
            GiftCardModel updated = giftCardRepository.save(giftCard);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating gift card: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/gift-cards/{id}")
    @Operation(summary = "Удалить подарочную карту")
    public ResponseEntity<Void> deleteGiftCard(@PathVariable Long id, Authentication authentication) {
        try {
            if (!giftCardRepository.existsById(id)) return ResponseEntity.notFound().build();
            giftCardRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting gift card: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/comments")
    @Operation(summary = "Получить все комментарии")
    public ResponseEntity<List<CommentModel>> getAllComments(Authentication authentication) {
        try {
            List<CommentModel> comments = commentRepository.findAll();
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/comments/{id}")
    @Operation(summary = "Получить комментарий по ID")
    public ResponseEntity<CommentModel> getCommentById(@PathVariable Long id, Authentication authentication) {
        try {
            CommentModel comment = commentRepository.findById(id).orElse(null);
            if (comment == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/comments")
    @Operation(summary = "Создать комментарий")
    public ResponseEntity<CommentModel> createComment(@RequestBody CommentModel comment, Authentication authentication) {
        try {
            CommentModel saved = commentRepository.save(comment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/comments/{id}")
    @Operation(summary = "Обновить комментарий")
    public ResponseEntity<CommentModel> updateComment(@PathVariable Long id, @RequestBody CommentModel comment, Authentication authentication) {
        try {
            CommentModel existing = commentRepository.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            comment.setCommentId(id);
            CommentModel updated = commentRepository.save(comment);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Удалить комментарий")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Authentication authentication) {
        try {
            if (!commentRepository.existsById(id)) return ResponseEntity.notFound().build();
            commentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/roles")
    @Operation(summary = "Получить все роли")
    public ResponseEntity<List<RoleModel>> getAllRoles(Authentication authentication) {
        try {
            return ResponseEntity.ok(roleRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/course-statuses")
    @Operation(summary = "Получить все статусы курсов")
    public ResponseEntity<List<CourseStatusModel>> getAllCourseStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(courseStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/lesson-statuses")
    @Operation(summary = "Получить все статусы уроков")
    public ResponseEntity<List<LessonStatusModel>> getAllLessonStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(lessonStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/task-statuses")
    @Operation(summary = "Получить все статусы заданий")
    public ResponseEntity<List<TaskStatusModel>> getAllTaskStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(taskStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/solution-statuses")
    @Operation(summary = "Получить все статусы решений")
    public ResponseEntity<List<SolutionStatusModel>> getAllSolutionStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(solutionStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payment-statuses")
    @Operation(summary = "Получить все статусы платежей")
    public ResponseEntity<List<PaymentStatusModel>> getAllPaymentStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(paymentStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/gift-card-statuses")
    @Operation(summary = "Получить все статусы подарочных карт")
    public ResponseEntity<List<GiftCardStatusModel>> getAllGiftCardStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(giftCardStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/certificate-statuses")
    @Operation(summary = "Получить все статусы сертификатов")
    public ResponseEntity<List<CertificateStatusModel>> getAllCertificateStatuses(Authentication authentication) {
        try {
            return ResponseEntity.ok(certificateStatusRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/export/users/csv")
    @Operation(summary = "Экспорт пользователей в CSV", description = "Экспорт всех пользователей в формате CSV")
    public ResponseEntity<byte[]> exportUsersToCsv(Authentication authentication) {
        try {
            byte[] csvData = exportImportService.exportUsersToCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "users_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            logger.error("Error exporting users to CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/export/courses/csv")
    @Operation(summary = "Экспорт курсов в CSV", description = "Экспорт всех курсов в формате CSV")
    public ResponseEntity<byte[]> exportCoursesToCsv(Authentication authentication) {
        try {
            byte[] csvData = exportImportService.exportCoursesToCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "courses_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            logger.error("Error exporting courses to CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/export/enrollments/csv")
    @Operation(summary = "Экспорт записей на курсы в CSV", description = "Экспорт всех записей на курсы в формате CSV")
    public ResponseEntity<byte[]> exportEnrollmentsToCsv(Authentication authentication) {
        try {
            byte[] csvData = exportImportService.exportEnrollmentsToCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "enrollments_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            logger.error("Error exporting enrollments to CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/export/statistics/csv")
    @Operation(summary = "Экспорт статистики по курсам в CSV", description = "Экспорт статистики по всем курсам в формате CSV")
    public ResponseEntity<byte[]> exportCourseStatisticsToCsv(Authentication authentication) {
        try {
            byte[] csvData = exportImportService.exportCourseStatisticsToCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "course_statistics_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            logger.error("Error exporting course statistics to CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/import/users/csv")
    @Operation(summary = "Импорт пользователей из CSV", description = "Импорт пользователей из файла CSV")
    public ResponseEntity<ExportImportService.ImportResult> importUsersFromCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                ExportImportService.ImportResult result = new ExportImportService.ImportResult();
                result.addError("Файл пуст");
                return ResponseEntity.badRequest().body(result);
            }
            ExportImportService.ImportResult result = exportImportService.importUsersFromCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error importing users from CSV: {}", e.getMessage(), e);
            ExportImportService.ImportResult result = new ExportImportService.ImportResult();
            result.addError("Ошибка импорта: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/import/courses/csv")
    @Operation(summary = "Импорт курсов из CSV", description = "Импорт курсов из файла CSV")
    public ResponseEntity<ExportImportService.ImportResult> importCoursesFromCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                ExportImportService.ImportResult result = new ExportImportService.ImportResult();
                result.addError("Файл пуст");
                return ResponseEntity.badRequest().body(result);
            }
            ExportImportService.ImportResult result = exportImportService.importCoursesFromCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error importing courses from CSV: {}", e.getMessage(), e);
            ExportImportService.ImportResult result = new ExportImportService.ImportResult();
            result.addError("Ошибка импорта: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}