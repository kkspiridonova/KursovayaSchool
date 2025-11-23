package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import com.example.OnlineSchoolKursach.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/comments")
@Tag(name = "Comment Management", description = "API для управления комментариями")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Получить комментарии к заданию", description = "Получение всех комментариев к заданию с поддержкой вложенных ответов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарии успешно получены"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к заданию")
    })
    public ResponseEntity<List<CommentModel>> getTaskComments(
            @Parameter(description = "ID задания") @PathVariable Long taskId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).build();
            }

            TaskModel task = taskRepository.findByIdWithCourseAndTeacher(taskId).orElse(null);
            if (task == null) {
                logger.warn("Task {} not found", taskId);
                return ResponseEntity.notFound().build();
            }

            UserModel user = authService.getUserByEmail(authentication.getName());
            logger.info("Checking access for user {} (email: {}, ID: {}) to task {}", 
                user.getEmail(), user.getEmail(), user.getUserId(), taskId);

            if (task.getLesson() != null && task.getLesson().getCourse() != null) {
                CourseModel course = task.getLesson().getCourse();
                logger.info("Course ID: {}, Teacher: {}", 
                    course.getCourseId(), 
                    course.getTeacher() != null ? course.getTeacher().getEmail() + " (ID: " + course.getTeacher().getUserId() + ")" : "null");
            }
            
            if (!hasAccessToTask(user, task)) {
                logger.warn("User {} (ID: {}) does not have access to task {}", user.getEmail(), user.getUserId(), taskId);
                if (task.getLesson() != null && task.getLesson().getCourse() != null) {
                    CourseModel course = task.getLesson().getCourse();
                    logger.warn("Course teacher ID: {}, User ID: {}", 
                        course.getTeacher() != null ? course.getTeacher().getUserId() : "null", 
                        user.getUserId());
                }
                return ResponseEntity.status(403).build();
            }

            List<CommentModel> comments = commentRepository.findByTaskTaskId(taskId);
            logger.info("Found {} comments for task {}", comments.size(), taskId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("Error getting task comments: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/task/{taskId}")
    @Operation(summary = "Создать комментарий к заданию", description = "Создание нового комментария к заданию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно создан"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к заданию")
    })
    public ResponseEntity<CommentModel> createTaskComment(
            @Parameter(description = "ID задания") @PathVariable Long taskId,
            @RequestBody Map<String, Object> commentData,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).build();
            }

            TaskModel task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }

            UserModel user = authService.getUserByEmail(authentication.getName());
            if (!hasAccessToTask(user, task)) {
                return ResponseEntity.status(403).build();
            }

            CommentModel comment = new CommentModel();
            comment.setUser(user);
            comment.setTask(task);
            comment.setText((String) commentData.get("text"));
            comment.setCreatedAt(LocalDateTime.now());

            if (commentData.containsKey("parentCommentId") && commentData.get("parentCommentId") != null) {
                Long parentId = Long.valueOf(commentData.get("parentCommentId").toString());
                CommentModel parentComment = commentRepository.findById(parentId).orElse(null);
                if (parentComment != null && parentComment.getTask() != null && 
                    parentComment.getTask().getTaskId().equals(taskId)) {
                    comment.setParentComment(parentComment);
                }
            }

            CommentModel saved = commentRepository.save(comment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error creating task comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удалить комментарий", description = "Удаление комментария (только автор или преподаватель курса)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно удален"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID комментария") @PathVariable Long commentId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(403).build();
            }

            CommentModel comment = commentRepository.findById(commentId).orElse(null);
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }

            UserModel user = authService.getUserByEmail(authentication.getName());

            boolean canDelete = comment.getUser().getUserId().equals(user.getUserId());
            
            if (!canDelete && comment.getTask() != null) {
                TaskModel task = comment.getTask();
                if (task.getLesson() != null && task.getLesson().getCourse() != null) {
                    CourseModel course = task.getLesson().getCourse();
                    canDelete = course.getTeacher().getUserId().equals(user.getUserId());
                }
            }

            if (!canDelete) {
                return ResponseEntity.status(403).build();
            }

            commentRepository.deleteById(commentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean hasAccessToTask(UserModel user, TaskModel task) {
        if (task == null || task.getLesson() == null || task.getLesson().getCourse() == null) {
            logger.warn("Task or related entities are null");
            return false;
        }

        CourseModel course = task.getLesson().getCourse();

        if (course.getTeacher() != null && course.getTeacher().getUserId().equals(user.getUserId())) {
            logger.info("User {} is teacher of course {}, granting access", user.getEmail(), course.getCourseId());
            return true;
        }

        List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
        boolean hasAccess = enrollments.stream()
                .anyMatch(enrollment -> enrollment.getUser().getUserId().equals(user.getUserId()) &&
                        enrollment.getEnrollmentStatus() != null &&
                        "Активный".equals(enrollment.getEnrollmentStatus().getStatusName()));
        
        if (hasAccess) {
            logger.info("User {} has active enrollment in course {}, granting access", user.getEmail(), course.getCourseId());
        } else {
            logger.warn("User {} does not have active enrollment in course {}", user.getEmail(), course.getCourseId());
        }
        
        return hasAccess;
    }
}

