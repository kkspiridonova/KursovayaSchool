package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.service.SolutionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/v1/api/solutions")
@Tag(name = "Solution Management", description = "API для управления решениями заданий")
public class SolutionController {

    private static final Logger logger = LoggerFactory.getLogger(SolutionController.class);

    @Autowired
    private SolutionService solutionService;
    
    @Autowired
    private AuthService authService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Получить решения задания", description = "Получение списка решений для указанного задания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список решений успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = SolutionModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<SolutionModel>> getSolutionsByTaskId(
            @Parameter(description = "Идентификатор задания") 
            @PathVariable Long taskId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            // Check if user is teacher of the course or admin
            SolutionModel sampleSolution = solutionService.getSolutionById(taskId);
            if (sampleSolution != null) {
                if (!sampleSolution.getTask().getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId()) &&
                    !"ADMIN".equals(user.getRole().getRoleName())) {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            List<SolutionModel> solutions = solutionService.getSolutionsByTaskId(taskId);
            return ResponseEntity.ok(solutions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои решения", description = "Получение списка решений текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список решений успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = SolutionModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<SolutionModel>> getMySolutions(
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<SolutionModel> solutions = solutionService.getSolutionsByUserId(user.getUserId());
            return ResponseEntity.ok(solutions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{solutionId}")
    @Operation(summary = "Получить детали решения", description = "Получение подробной информации о решении")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о решении успешно получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = SolutionModel.class))),
            @ApiResponse(responseCode = "404", description = "Решение не найдено")
    })
    public ResponseEntity<SolutionModel> getSolutionById(
            @Parameter(description = "Идентификатор решения") 
            @PathVariable Long solutionId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            SolutionModel solution = solutionService.getSolutionById(solutionId);
            
            if (solution != null) {
                // Check if user is owner of solution or teacher of the course or admin
                if (!solution.getUser().getUserId().equals(user.getUserId()) &&
                    !solution.getTask().getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId()) &&
                    !"ADMIN".equals(user.getRole().getRoleName())) {
                    return ResponseEntity.badRequest().build();
                }
                
                return ResponseEntity.ok(solution);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Создать решение", description = "Создание нового решения задания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Решение успешно создано", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = SolutionModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<?> createSolution(
            @Parameter(description = "Данные нового решения") 
            @RequestBody SolutionModel solution,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            logger.info("=== CREATE SOLUTION REQUEST ===");
            logger.info("Solution data: taskId={}, answerText={}, answerFile={}", 
                solution.getTask() != null ? solution.getTask().getTaskId() : "null",
                solution.getAnswerText() != null ? "present" : "null",
                solution.getAnswerFile() != null ? solution.getAnswerFile() : "null");
            
            UserModel user = authService.getUserByEmail(authentication.getName());
            logger.info("User loaded: {}", user.getUserId());
            solution.setUser(user);
            
            // Check if solution already exists for this user and task
            if (solution.getTask() != null && solution.getTask().getTaskId() != null) {
                SolutionModel existingSolution = solutionService.getSolutionByTaskAndUser(
                    solution.getTask().getTaskId(), user.getUserId());
                if (existingSolution != null) {
                    // Always merge into existing solution to avoid duplicate errors
                    logger.info("Existing solution found. Merging incoming data into solutionId={}", existingSolution.getSolutionId());
                    if (solution.getAnswerText() != null) {
                        existingSolution.setAnswerText(solution.getAnswerText());
                    }
                    if (solution.getAnswerFile() != null) {
                        existingSolution.setAnswerFile(solution.getAnswerFile());
                    }
                    SolutionModel updated = solutionService.updateSolution(existingSolution.getSolutionId(), existingSolution);
                    return ResponseEntity.ok(updated);
                }
            }
            
            logger.info("Calling solutionService.createSolution()...");
            SolutionModel createdSolution = solutionService.createSolution(solution);
            logger.info("Solution created successfully with ID: {}", createdSolution.getSolutionId());
            return ResponseEntity.ok(createdSolution);
        } catch (RuntimeException e) {
            // Return error message for business logic errors
            logger.error("RuntimeException in createSolution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in createSolution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при создании решения: " + e.getMessage());
        }
    }

    @PutMapping("/{solutionId}")
    @Operation(summary = "Обновить решение", description = "Обновление информации о решении")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Решение успешно обновлено", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = SolutionModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Решение не найдено")
    })
    public ResponseEntity<SolutionModel> updateSolution(
            @Parameter(description = "Идентификатор решения") 
            @PathVariable Long solutionId,
            @Parameter(description = "Обновленные данные решения") 
            @RequestBody SolutionModel solution,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            SolutionModel existingSolution = solutionService.getSolutionById(solutionId);
            
            if (existingSolution == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user is owner of solution
            if (!existingSolution.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Only allow updating answer text and file
            existingSolution.setAnswerText(solution.getAnswerText());
            existingSolution.setAnswerFile(solution.getAnswerFile());
            
            SolutionModel updatedSolution = solutionService.updateSolution(solutionId, existingSolution);
            if (updatedSolution != null) {
                return ResponseEntity.ok(updatedSolution);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{solutionId}")
    @Operation(summary = "Удалить решение", description = "Удаление решения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Решение успешно удалено"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Решение не найдено")
    })
    public ResponseEntity<Void> deleteSolution(
            @Parameter(description = "Идентификатор решения") 
            @PathVariable Long solutionId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            SolutionModel existingSolution = solutionService.getSolutionById(solutionId);
            
            if (existingSolution == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user is owner of solution
            if (!existingSolution.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean deleted = solutionService.deleteSolution(solutionId);
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