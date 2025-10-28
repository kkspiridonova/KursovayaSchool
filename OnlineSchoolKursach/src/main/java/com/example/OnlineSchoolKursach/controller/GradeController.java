package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.GradeModel;
import com.example.OnlineSchoolKursach.service.GradeService;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.SolutionService;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.model.SolutionModel;

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

@RestController
@RequestMapping("/v1/api/grades")
@Tag(name = "Grade Management", description = "API для управления оценками")
public class GradeController {

    @Autowired
    private GradeService gradeService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private SolutionService solutionService;

    @GetMapping("/{gradeId}")
    @Operation(summary = "Получить детали оценки", description = "Получение подробной информации об оценке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация об оценке успешно получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = GradeModel.class))),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    public ResponseEntity<GradeModel> getGradeById(
            @Parameter(description = "Идентификатор оценки") 
            @PathVariable Long gradeId) {
        try {
            GradeModel grade = gradeService.getGradeById(gradeId);
            if (grade != null) {
                return ResponseEntity.ok(grade);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Создать оценку", description = "Создание новой оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка успешно создана", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = GradeModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<GradeModel> createGrade(
            @Parameter(description = "Данные новой оценки") 
            @RequestBody GradeModel grade,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            
            // Load the full solution object from database
            SolutionModel solution = null;
            if (grade.getSolution() != null && grade.getSolution().getSolutionId() != null) {
                solution = solutionService.getSolutionById(grade.getSolution().getSolutionId());
            }
            
            // Check if solution exists and user is teacher of the course
            if (solution == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Verify the user is the teacher of the course
            if (!solution.getTask().getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Set the full solution object in the grade (without null check - we've already verified)
            grade.setSolution(solution);
            
            // Check if grade already exists for this solution
            if (solution.getGrade() != null) {
                // Update existing grade instead of creating new one
                GradeModel existingGrade = solution.getGrade();
                existingGrade.setGradeValue(grade.getGradeValue());
                existingGrade.setFeedback(grade.getFeedback());
                GradeModel updatedGrade = gradeService.updateGrade(existingGrade.getGradeId(), existingGrade);
                return ResponseEntity.ok(updatedGrade);
            } else {
                // Create new grade
                GradeModel createdGrade = gradeService.createGrade(grade);
                return ResponseEntity.ok(createdGrade);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{gradeId}")
    @Operation(summary = "Обновить оценку", description = "Обновление информации об оценке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка успешно обновлена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = GradeModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    public ResponseEntity<GradeModel> updateGrade(
            @Parameter(description = "Идентификатор оценки") 
            @PathVariable Long gradeId,
            @Parameter(description = "Обновленные данные оценки") 
            @RequestBody GradeModel grade,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            // Load the existing grade from database
            GradeModel existingGrade = gradeService.getGradeById(gradeId);
            if (existingGrade == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user is teacher of the course
            if (!existingGrade.getSolution().getTask().getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            GradeModel updatedGrade = gradeService.updateGrade(gradeId, grade);
            if (updatedGrade != null) {
                return ResponseEntity.ok(updatedGrade);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{gradeId}")
    @Operation(summary = "Удалить оценку", description = "Удаление оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка успешно удалена"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    public ResponseEntity<Void> deleteGrade(
            @Parameter(description = "Идентификатор оценки") 
            @PathVariable Long gradeId,
            @Parameter(description = "Данные аутентификации") 
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            // Load the existing grade from database
            GradeModel existingGrade = gradeService.getGradeById(gradeId);
            if (existingGrade == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user is teacher of the course
            if (!existingGrade.getSolution().getTask().getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId())) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean deleted = gradeService.deleteGrade(gradeId);
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