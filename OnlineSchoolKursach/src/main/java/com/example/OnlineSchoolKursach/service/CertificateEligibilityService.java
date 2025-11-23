package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CertificateEligibilityService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateEligibilityService.class);

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
        logger.debug("Checking eligibility for user {} (ID: {}) for course {} (ID: {})", 
                user.getEmail(), user.getUserId(), course.getTitle(), course.getCourseId());
        
        if (course.getEndDate() == null) {
            logger.debug("Course {} has no end date", course.getTitle());
            return false;
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(course.getEndDate())) {
            logger.debug("Course {} end date {} is in the future (today: {})", 
                    course.getTitle(), course.getEndDate(), today);
                return false;
        }

        if (certificateRepository.findByUserUserIdAndCourseCourseId(user.getUserId(), course.getCourseId()).isPresent()) {
            logger.debug("Certificate already exists for user {} course {}", user.getEmail(), course.getTitle());
            return false;
        }

        List<LessonModel> lessons = lessonRepository.findByCourseCourseId(course.getCourseId());
        if (lessons.isEmpty()) {
            logger.debug("Course {} has no lessons", course.getTitle());
            return false;
        }

        boolean hasAnySolution = false;
        int totalTasks = 0;
        int completedTasks = 0;

        for (LessonModel lesson : lessons) {
            List<TaskModel> tasks = taskRepository.findByLessonLessonId(lesson.getLessonId());
            
            if (tasks.isEmpty()) {
                logger.debug("Lesson {} has no tasks, skipping", lesson.getLessonId());
                continue;
            }
            
            for (TaskModel task : tasks) {
                totalTasks++;
                SolutionModel solution = solutionRepository.findFirstByTaskTaskIdAndUserUserId(
                        task.getTaskId(), user.getUserId());
                
                if (solution != null) {
                    hasAnySolution = true;
                    if (solution.getGrade() != null) {
                        completedTasks++;
                        logger.debug("Task {} has solution with grade for user {}", task.getTaskId(), user.getEmail());
                    } else {
                        logger.debug("Task {} has solution but no grade for user {}", task.getTaskId(), user.getEmail());
                    }
                } else {
                    logger.debug("Task {} has no solution for user {}", task.getTaskId(), user.getEmail());
                }
            }
        }

        logger.debug("User {} course {}: totalTasks={}, completedTasks={}, hasAnySolution={}", 
                user.getEmail(), course.getTitle(), totalTasks, completedTasks, hasAnySolution);

        if (!hasAnySolution) {
            logger.debug("User {} has no solutions for course {}", user.getEmail(), course.getTitle());
            return false;
                }

        if (totalTasks == 0) {
            logger.debug("Course {} has no tasks", course.getTitle());
            return false;
        }

        boolean eligible = completedTasks == totalTasks;
        logger.info("User {} eligibility for course {}: {} (completed {}/{} tasks)", 
                user.getEmail(), course.getTitle(), eligible ? "ELIGIBLE" : "NOT ELIGIBLE", completedTasks, totalTasks);
        
        return eligible;
    }
}