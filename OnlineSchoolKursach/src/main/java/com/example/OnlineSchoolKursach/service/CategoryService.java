package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.CategoryModel;
import com.example.OnlineSchoolKursach.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryModel> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<CategoryModel> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public CategoryModel createCategory(CategoryModel category) {
        return categoryRepository.save(category);
    }

    public CategoryModel updateCategory(CategoryModel category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}