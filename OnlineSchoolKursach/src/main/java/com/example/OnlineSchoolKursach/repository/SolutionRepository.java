package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.SolutionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<SolutionModel, Long> {
    @Query("SELECT DISTINCT s FROM SolutionModel s LEFT JOIN FETCH s.grade WHERE s.task.taskId = :taskId")
    List<SolutionModel> findByTaskTaskIdWithGrade(@Param("taskId") Long taskId);
    
    List<SolutionModel> findByTaskTaskId(Long taskId);
    List<SolutionModel> findByUserUserId(Long userId);
    List<SolutionModel> findBySolutionStatusSolutionStatusId(Long statusId);

    // Precise lookup to detect if a user's solution for a specific task already exists
    @Query("SELECT DISTINCT s FROM SolutionModel s LEFT JOIN FETCH s.grade WHERE s.task.taskId = :taskId AND s.user.userId = :userId")
    SolutionModel findFirstByTaskTaskIdAndUserUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    
    // Find all solutions with distinct to avoid duplicate rows from JOINs
    // Используем загрузку только ID, чтобы избежать дубликатов от JOIN'ов
    @Query("SELECT DISTINCT s.solutionId FROM SolutionModel s")
    List<Long> findAllDistinctIds();
}