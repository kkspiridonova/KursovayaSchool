package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.CommentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentModel, Long> {
    List<CommentModel> findByUserUserId(Long userId);
    List<CommentModel> findByLessonLessonId(Long lessonId);
    List<CommentModel> findByTaskTaskId(Long taskId);
    List<CommentModel> findByParentCommentCommentId(Long parentCommentId);
}

