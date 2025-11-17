package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.service.TaskService;
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
@RequestMapping("/v1/api/tasks")
@Tag(name = "Task Management", description = "API для управления заданиями")
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AuthService authService;

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Получить задания урока", description = "Получение списка заданий для указанного урока")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заданий успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = TaskModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<TaskModel>> getTasksByLessonId(
            @Parameter(description = "Идентификатор урока") 
            @PathVariable Long lessonId) {
        try {
            List<TaskModel> tasks = taskService.getTasksByLessonId(lessonId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Получить детали задания", description = "Получение подробной информации о задании")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о задании успешно получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = TaskModel.class))),
            @ApiResponse(responseCode = "404", description = "Задание не найдено")
    })
    public ResponseEntity<TaskModel> getTaskById(
            @Parameter(description = "Идентификатор задания") 
            @PathVariable Long taskId) {
        try {
            TaskModel task = taskService.getTaskById(taskId);
            if (task != null) {
                return ResponseEntity.ok(task);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Создать задание", description = "Создание нового задания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задание успешно создано", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = TaskModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<TaskModel> createTask(
            @Parameter(description = "Данные нового задания") 
            @RequestBody TaskModel task,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());

            if (!task.getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            TaskModel createdTask = taskService.createTask(task);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Обновить задание", description = "Обновление информации о задании")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задание успешно обновлено", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = TaskModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Задание не найдено")
    })
    public ResponseEntity<TaskModel> updateTask(
            @Parameter(description = "Идентификатор задания") 
            @PathVariable Long taskId,
            @Parameter(description = "Обновленные данные задания") 
            @RequestBody TaskModel task,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());

            TaskModel existingTask = taskService.getTaskById(taskId);
            if (existingTask == null || 
                !existingTask.getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            TaskModel updatedTask = taskService.updateTask(taskId, task, null);
            if (updatedTask != null) {
                return ResponseEntity.ok(updatedTask);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Удалить задание", description = "Удаление задания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задание успешно удалено"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Задание не найдено")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Идентификатор задания") 
            @PathVariable Long taskId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());

            TaskModel existingTask = taskService.getTaskById(taskId);
            if (existingTask == null || 
                !existingTask.getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean deleted = taskService.deleteTask(taskId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my/calendar")
    @Operation(summary = "Получить задания студента для календаря", description = "Получение заданий студента с дедлайнами для отображения в календаре")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заданий успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = TaskModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<TaskModel>> getMyTasksForCalendar(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication,
            @Parameter(description = "Начало недели (YYYY-MM-DD)") 
            @RequestParam(required = false) String weekStart) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<TaskModel> tasks;
            
            if (weekStart != null && !weekStart.isEmpty()) {
                java.time.LocalDate weekStartDate = java.time.LocalDate.parse(weekStart);
                tasks = taskService.getStudentTasksForWeek(user, weekStartDate);
            } else {
                tasks = taskService.getStudentTasks(user);
            }
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/calendar")
    @Operation(summary = "Получить задания преподавателя для календаря", description = "Получение заданий преподавателя с дедлайнами для отображения в календаре")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заданий успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = TaskModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<TaskModel>> getTeacherTasksForCalendar(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication,
            @Parameter(description = "Начало недели (YYYY-MM-DD)") 
            @RequestParam(required = false) String weekStart) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<TaskModel> tasks;
            
            if (weekStart != null && !weekStart.isEmpty()) {
                java.time.LocalDate weekStartDate = java.time.LocalDate.parse(weekStart);
                tasks = taskService.getTeacherTasksForWeek(user, weekStartDate);
            } else {
                tasks = taskService.getTeacherTasks(user);
            }
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}