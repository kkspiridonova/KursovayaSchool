package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.LessonModel;
import com.example.OnlineSchoolKursach.model.LessonStatusModel;
import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.repository.LessonRepository;
import com.example.OnlineSchoolKursach.repository.LessonStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private LessonStatusRepository lessonStatusRepository;
    
    @Autowired
    private MinioFileService minioFileService;

    public List<LessonModel> getLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourseCourseId(courseId);
    }

    public LessonModel getLessonById(Long lessonId) {
        Optional<LessonModel> lesson = lessonRepository.findById(lessonId);
        return lesson.orElse(null);
    }

    public LessonModel createLesson(LessonModel lesson) {
        // Set default status if not provided
        if (lesson.getLessonStatus() == null) {
            List<LessonStatusModel> statuses = lessonStatusRepository.findAll();
            if (!statuses.isEmpty()) {
                lesson.setLessonStatus(statuses.get(0)); // Default to first status
            }
        }
        return lessonRepository.save(lesson);
    }

    public LessonModel updateLesson(Long lessonId, LessonModel updatedLesson, MultipartFile newFile) {
        Optional<LessonModel> existingLessonOpt = lessonRepository.findById(lessonId);
        if (existingLessonOpt.isPresent()) {
            LessonModel existingLesson = existingLessonOpt.get();
            existingLesson.setTitle(updatedLesson.getTitle());
            existingLesson.setContent(updatedLesson.getContent());
            existingLesson.setLessonStatus(updatedLesson.getLessonStatus());
            
            // Handle file update
            if (newFile != null && !newFile.isEmpty()) {
                // Delete old file if exists
                if (existingLesson.getAttachedFile() != null && !existingLesson.getAttachedFile().isEmpty()) {
                    try {
                        minioFileService.deleteFile(existingLesson.getAttachedFile());
                    } catch (Exception e) {
                        // Log error but continue
                        System.err.println("Error deleting old lesson file: " + e.getMessage());
                    }
                }
                // Upload new file
                try {
                    String filePath = minioFileService.uploadFile(newFile, "lesson");
                    existingLesson.setAttachedFile(filePath);
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при загрузке файла: " + e.getMessage());
                }
            } else if (updatedLesson.getAttachedFile() != null && !updatedLesson.getAttachedFile().isEmpty()) {
                // Keep existing file path if new file not provided
                existingLesson.setAttachedFile(updatedLesson.getAttachedFile());
            }
            
            return lessonRepository.save(existingLesson);
        }
        return null;
    }

    public boolean deleteLesson(Long lessonId) {
        Optional<LessonModel> lessonOpt = lessonRepository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            LessonModel lesson = lessonOpt.get();
            
            // Delete attached file from MinIO if exists
            if (lesson.getAttachedFile() != null && !lesson.getAttachedFile().isEmpty()) {
                try {
                    minioFileService.deleteFile(lesson.getAttachedFile());
                } catch (Exception e) {
                    System.err.println("Error deleting lesson file: " + e.getMessage());
                }
            }
            
            lessonRepository.deleteById(lessonId);
            return true;
        }
        return false;
    }
}