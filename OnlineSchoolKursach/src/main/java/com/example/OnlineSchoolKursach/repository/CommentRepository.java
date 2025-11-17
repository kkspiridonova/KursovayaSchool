package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CommentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentModel, Long> {
    List<CommentModel> findByUserUserId(Long userId);
    List<CommentModel> findByLessonLessonId(Long lessonId);
    
    @Query("SELECT DISTINCT c FROM CommentModel c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.parentComment " +
           "WHERE c.task.taskId = :taskId " +
           "ORDER BY c.createdAt ASC")
    List<CommentModel> findByTaskTaskId(@Param("taskId") Long taskId);
    
    List<CommentModel> findByParentCommentCommentId(Long parentCommentId);
}

