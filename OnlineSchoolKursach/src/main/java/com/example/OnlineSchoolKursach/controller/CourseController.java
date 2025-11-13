package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.EnrollmentModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.model.CheckModel;

import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.CheckService;
import com.example.OnlineSchoolKursach.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/courses")
@Tag(name = "Course Management", description = "API для управления курсами")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CheckService checkService;

    @GetMapping
    @Operation(summary = "Получить все курсы", description = "Получение списка всех доступных курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список курсов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class)))
    })
    public ResponseEntity<List<CourseModel>> getAllCourses() {
        List<CourseModel> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои курсы", description = "Получение списка курсов, на которые записан текущий пользователь")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список курсов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<CourseModel>> getMyCourses(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<CourseModel> courses = courseService.getEnrolledCourses(user);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/all")
    @Operation(summary = "Получить все курсы преподавателя", description = "Получение списка всех курсов преподавателя, включая архивные")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список курсов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<CourseModel>> getAllCoursesByTeacher(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<CourseModel> courses = courseService.getAllCoursesByTeacher(user);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my/enrollments")
    @Operation(summary = "Получить все записи пользователя", description = "Получение списка всех записей текущего пользователя на курсы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список записей успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<EnrollmentModel>> getMyEnrollments(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<EnrollmentModel> enrollments = courseService.getUserEnrollments(user);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Получить курсы по категории", description = "Получение списка курсов по указанной категории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список курсов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<CourseModel>> getCoursesByCategory(
            @Parameter(description = "Идентификатор категории") 
            @PathVariable Long categoryId) {
        try {
            List<CourseModel> courses = courseService.getCoursesByCategory(categoryId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Получить детали курса", description = "Получение подробной информации о курсе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о курсе успешно получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class))),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    public ResponseEntity<CourseModel> getCourseById(
            @Parameter(description = "Идентификатор курса") 
            @PathVariable Long courseId) {
        try {
            CourseModel course = courseService.getCourseById(courseId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my/checks")
    @Operation(summary = "Получить все чеки пользователя", description = "Получение списка всех чеков текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список чеков успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CheckModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<CheckModel>> getMyChecks(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<CheckModel> checks = checkService.getChecksByUser(user);
            return ResponseEntity.ok(checks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(summary = "Создать курс", description = "Создание нового курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Курс успешно создан", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<CourseModel> createCourse(
            @Parameter(description = "Данные нового курса") 
            @RequestBody CourseModel course,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            course.setTeacher(user);
            CourseModel createdCourse = courseService.createCourse(course);
            return ResponseEntity.ok(createdCourse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Обновить курс", description = "Обновление информации о курсе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Курс успешно обновлен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    public ResponseEntity<CourseModel> updateCourse(
            @Parameter(description = "Идентификатор курса") 
            @PathVariable Long courseId,
            @Parameter(description = "Обновленные данные курса") 
            @RequestBody CourseModel course,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            CourseModel updatedCourse = courseService.updateCourse(courseId, course, user);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{courseId}/enroll")
    @Operation(summary = "Записаться на курс", description = "Запись текущего пользователя на указанный курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно записан на курс", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnrollmentModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    public ResponseEntity<?> enrollInCourse(
            @Parameter(description = "Идентификатор курса") 
            @PathVariable Long courseId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            EnrollmentModel enrollment = courseService.enrollUserInCourse(user, courseId);
            return ResponseEntity.ok(enrollment);
        } catch (Exception e) {
            // Return error message in response body
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Ошибка при записи на курс");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}