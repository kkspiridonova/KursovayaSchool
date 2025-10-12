package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskModel, Long> {
    List<TaskModel> findByLessonLessonId(Long lessonId);
    List<TaskModel> findByTaskStatusTaskStatusId(Long statusId);
    List<TaskModel> findByDeadlineBefore(LocalDate date);
}
