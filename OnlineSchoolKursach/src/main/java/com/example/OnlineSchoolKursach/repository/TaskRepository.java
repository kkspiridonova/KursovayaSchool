package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskModel, Long> {
    List<TaskModel> findByLessonLessonId(Long lessonId);
    List<TaskModel> findByTaskStatusTaskStatusId(Long statusId);
    List<TaskModel> findByDeadlineBefore(LocalDate date);
    
    @Query("SELECT t FROM TaskModel t " +
           "LEFT JOIN FETCH t.lesson l " +
           "LEFT JOIN FETCH l.course c " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE t.taskId = :taskId")
    Optional<TaskModel> findByIdWithCourseAndTeacher(@Param("taskId") Long taskId);
}

