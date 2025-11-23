package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificateEligibilityService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Transactional(readOnly = true)
    public boolean isEligibleForCertificate(UserModel user, CourseModel course) {
        if (course.getEndDate() == null || LocalDate.now().isBefore(course.getEndDate())) {
            return false;
        }

        List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
        boolean isEnrolled = enrollments.stream()
                .anyMatch(e -> e.getUser().getUserId().equals(user.getUserId()) 
                        && e.getEnrollmentStatus() != null 
                        && "Активный".equals(e.getEnrollmentStatus().getStatusName()));

        if (!isEnrolled) {
            List<LessonModel> lessons = lessonRepository.findByCourseCourseId(course.getCourseId());
            boolean hasAnySolution = false;
            for (LessonModel lesson : lessons) {
                List<TaskModel> tasks = taskRepository.findByLessonLessonId(lesson.getLessonId());
                for (TaskModel task : tasks) {
                    SolutionModel solution = solutionRepository.findFirstByTaskTaskIdAndUserUserId(
                            task.getTaskId(), user.getUserId());
                    if (solution != null) {
                        hasAnySolution = true;
                        break;
                    }
                }
                if (hasAnySolution) break;
            }
            if (!hasAnySolution) {
                return false;
            }
        }

        if (certificateRepository.findByUserUserIdAndCourseCourseId(user.getUserId(), course.getCourseId()).isPresent()) {
            return false;
        }

        List<LessonModel> lessons = lessonRepository.findByCourseCourseId(course.getCourseId());
        if (lessons.isEmpty()) {
            return false;
        }

        for (LessonModel lesson : lessons) {
            List<TaskModel> tasks = taskRepository.findByLessonLessonId(lesson.getLessonId());
            
            for (TaskModel task : tasks) {
                SolutionModel solution = solutionRepository.findFirstByTaskTaskIdAndUserUserId(
                        task.getTaskId(), user.getUserId());
                
                if (solution == null) {
                    return false;
                }

                if (solution.getGrade() == null) {
                    return false;
                }
            }
        }

        return true;
    }
}