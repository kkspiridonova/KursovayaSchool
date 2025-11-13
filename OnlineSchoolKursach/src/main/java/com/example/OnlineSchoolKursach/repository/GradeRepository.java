package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.GradeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<GradeModel, Long> {
    // Используем findFirst для получения первой записи, если есть дубликаты
    GradeModel findFirstByGradeValue(Integer gradeValue);
    
    // Также добавим метод для получения всех оценок с определенным значением (на случай если нужно)
    java.util.List<GradeModel> findAllByGradeValue(Integer gradeValue);
}

