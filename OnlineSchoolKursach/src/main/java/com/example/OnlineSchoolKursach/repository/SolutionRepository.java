package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.SolutionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<SolutionModel, Long> {
    @Query("SELECT s FROM SolutionModel s LEFT JOIN FETCH s.grade WHERE s.task.taskId = :taskId")
    List<SolutionModel> findByTaskTaskIdWithGrade(@Param("taskId") Long taskId);
    
    List<SolutionModel> findByTaskTaskId(Long taskId);
    List<SolutionModel> findByUserUserId(Long userId);
    List<SolutionModel> findBySolutionStatusSolutionStatusId(Long statusId);
}