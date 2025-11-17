package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    Optional<CategoryModel> findByCategoryName(String categoryName);
}