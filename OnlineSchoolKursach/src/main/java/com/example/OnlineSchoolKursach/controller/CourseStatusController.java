package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.CourseStatusModel;
import com.example.OnlineSchoolKursach.service.CourseStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/api/course-statuses")
@Tag(name = "Course Status Management", description = "API для управления статусами курсов")
public class CourseStatusController {

    @Autowired
    private CourseStatusService courseStatusService;

    @GetMapping
    @Operation(summary = "Получить все статусы курсов", description = "Получение списка всех доступных статусов курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список статусов курсов успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CourseStatusModel.class)))
    })
    public ResponseEntity<List<CourseStatusModel>> getAllCourseStatuses() {
        List<CourseStatusModel> statuses = courseStatusService.getAllCourseStatuses();
        return ResponseEntity.ok(statuses);
    }
}