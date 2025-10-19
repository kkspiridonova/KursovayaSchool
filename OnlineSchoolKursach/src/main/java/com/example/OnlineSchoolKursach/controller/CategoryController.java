package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.CategoryModel;
import com.example.OnlineSchoolKursach.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/categories")
@Tag(name = "Category Management", description = "API для управления категориями курсов")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Получить все категории", description = "Получение списка всех доступных категорий курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CategoryModel.class)))
    })
    public ResponseEntity<List<CategoryModel>> getAllCategories() {
        List<CategoryModel> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Получить категорию по ID", description = "Получение подробной информации о категории по её идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о категории успешно получена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CategoryModel.class))),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<CategoryModel> getCategoryById(
            @Parameter(description = "Идентификатор категории") 
            @PathVariable Long categoryId) {
        return categoryService.getCategoryById(categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Создать категорию", description = "Создание новой категории курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно создана", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CategoryModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<CategoryModel> createCategory(
            @Parameter(description = "Данные новой категории") 
            @RequestBody CategoryModel category) {
        try {
            CategoryModel createdCategory = categoryService.createCategory(category);
            return ResponseEntity.ok(createdCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Обновить категорию", description = "Обновление информации о категории курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно обновлена", 
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CategoryModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<CategoryModel> updateCategory(
            @Parameter(description = "Идентификатор категории") 
            @PathVariable Long categoryId,
            @Parameter(description = "Обновленные данные категории") 
            @RequestBody CategoryModel category) {
        try {
            category.setCategoryId(categoryId);
            CategoryModel updatedCategory = categoryService.updateCategory(category);
            return ResponseEntity.ok(updatedCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Удалить категорию", description = "Удаление категории курсов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Идентификатор категории") 
            @PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}