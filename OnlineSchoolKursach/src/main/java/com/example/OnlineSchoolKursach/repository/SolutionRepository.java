package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.SolutionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<SolutionModel, Long> {
    List<SolutionModel> findByTaskTaskId(Long taskId);
    List<SolutionModel> findByUserUserId(Long userId);
    List<SolutionModel> findBySolutionStatusSolutionStatusId(Long statusId);
}
