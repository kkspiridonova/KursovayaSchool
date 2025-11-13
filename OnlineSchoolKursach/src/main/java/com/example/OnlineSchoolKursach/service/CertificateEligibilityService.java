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

    /**
     * Проверяет, имеет ли студент право на получение сертификата за курс
     * 
     * @param user Студент
     * @param course Курс
     * @return true если студент имеет право на сертификат
     */
    @Transactional(readOnly = true)
    public boolean isEligibleForCertificate(UserModel user, CourseModel course) {
        // 1. Проверка, что курс закончился
        if (course.getEndDate() == null || LocalDate.now().isBefore(course.getEndDate())) {
            return false;
        }

        // 2. Проверка, что студент зачислен на курс
        // Примечание: enrollments могут быть удалены после окончания курса,
        // поэтому проверяем наличие решений как альтернативный способ проверки зачисления
        List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
        boolean isEnrolled = enrollments.stream()
                .anyMatch(e -> e.getUser().getUserId().equals(user.getUserId()) 
                        && e.getEnrollmentStatus() != null 
                        && "Активен".equals(e.getEnrollmentStatus().getStatusName()));
        
        // Если enrollments удалены, но есть решения - считаем что студент был зачислен
        if (!isEnrolled) {
            // Проверяем наличие хотя бы одного решения от студента по заданиям курса
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
                return false; // Нет ни зачисления, ни решений
            }
        }

        // 3. Проверка, что сертификат еще не выдан
        if (certificateRepository.findByUserUserIdAndCourseCourseId(user.getUserId(), course.getCourseId()).isPresent()) {
            return false; // Сертификат уже выдан
        }

        // 4. Получение всех уроков курса
        List<LessonModel> lessons = lessonRepository.findByCourseCourseId(course.getCourseId());
        if (lessons.isEmpty()) {
            return false; // Нет уроков - нет заданий
        }

        // 5. Проверка всех заданий курса
        for (LessonModel lesson : lessons) {
            List<TaskModel> tasks = taskRepository.findByLessonLessonId(lesson.getLessonId());
            
            for (TaskModel task : tasks) {
                // Проверка, есть ли решение от студента
                SolutionModel solution = solutionRepository.findFirstByTaskTaskIdAndUserUserId(
                        task.getTaskId(), user.getUserId());
                
                if (solution == null) {
                    return false; // Нет решения для задания
                }
                
                // Проверка, есть ли оценка у решения
                if (solution.getGrade() == null) {
                    return false; // Нет оценки за решение
                }
            }
        }

        return true; // Все условия выполнены
    }
}

