package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.TaskStatusModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatusModel, Long> {
}

