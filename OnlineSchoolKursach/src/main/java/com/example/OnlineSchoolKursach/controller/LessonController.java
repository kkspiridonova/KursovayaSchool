package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.LessonModel;
import com.example.OnlineSchoolKursach.service.LessonService;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.model.UserModel;
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
@RequestMapping("/v1/api/lessons")
@Tag(name = "Lesson Management", description = "API для управления уроками")
public class LessonController {

    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private AuthService authService;

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Получить уроки курса", description = "Получение списка уроков для указанного курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список уроков успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = LessonModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<LessonModel>> getLessonsByCourseId(
            @Parameter(description = "Идентификатор курса") 
            @PathVariable Long courseId) {
        try {
            List<LessonModel> lessons = lessonService.getLessonsByCourseId(courseId);
            return ResponseEntity.ok(lessons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Получить детали урока", description = "Получение подробной информации об уроке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация об уроке успешно получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = LessonModel.class))),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    public ResponseEntity<LessonModel> getLessonById(
            @Parameter(description = "Идентификатор урока") 
            @PathVariable Long lessonId) {
        try {
            LessonModel lesson = lessonService.getLessonById(lessonId);
            if (lesson != null) {
                return ResponseEntity.ok(lesson);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Создать урок", description = "Создание нового урока")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Урок успешно создан", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = LessonModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<LessonModel> createLesson(
            @Parameter(description = "Данные нового урока") 
            @RequestBody LessonModel lesson,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());

            if (!lesson.getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }

            String statusName = lesson.getCourse().getCourseStatus() != null ? lesson.getCourse().getCourseStatus().getStatusName() : null;
            if ("Идет набор".equals(statusName) || "Активный".equals(statusName)) {
                return ResponseEntity.badRequest().build();
            }
            
            LessonModel createdLesson = lessonService.createLesson(lesson);
            return ResponseEntity.ok(createdLesson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{lessonId}")
    @Operation(summary = "Обновить урок", description = "Обновление информации об уроке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Урок успешно обновлен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = LessonModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    public ResponseEntity<LessonModel> updateLesson(
            @Parameter(description = "Идентификатор урока") 
            @PathVariable Long lessonId,
            @Parameter(description = "Обновленные данные урока") 
            @RequestBody LessonModel lesson,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());

            LessonModel existingLesson = lessonService.getLessonById(lessonId);
            if (existingLesson == null || 
                !existingLesson.getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            LessonModel updatedLesson = lessonService.updateLesson(lessonId, lesson, null);
            if (updatedLesson != null) {
                return ResponseEntity.ok(updatedLesson);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "Удалить урок", description = "Удаление урока")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Урок успешно удален"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Урок не найден")
    })
    public ResponseEntity<Void> deleteLesson(
            @Parameter(description = "Идентификатор урока") 
            @PathVariable Long lessonId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());

            LessonModel existingLesson = lessonService.getLessonById(lessonId);
            if (existingLesson == null || 
                !existingLesson.getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean deleted = lessonService.deleteLesson(lessonId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}