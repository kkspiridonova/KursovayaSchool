package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CertificateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CertificateScheduler.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateEligibilityService eligibilityService;

    @Autowired
    private CertificateGenerationService generationService;

    @Autowired
    private CertificateEmailService emailService;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    private List<UserModel> getStudentsFromSolutions(CourseModel course) {
        try {
            List<LessonModel> lessons = lessonRepository.findByCourseCourseId(course.getCourseId());
            List<UserModel> students = new ArrayList<>();
            Set<Long> studentIds = new HashSet<>();
            
            for (LessonModel lesson : lessons) {
                List<TaskModel> tasks = taskRepository.findByLessonLessonId(lesson.getLessonId());
                for (TaskModel task : tasks) {
                    try {
                        List<SolutionModel> solutions = solutionRepository.findByTaskTaskIdWithGrade(task.getTaskId());
                        for (SolutionModel solution : solutions) {
                            if (solution.getUser() != null) {
                                Long userId = solution.getUser().getUserId();
                                if (userId != null && !studentIds.contains(userId)) {
                                    studentIds.add(userId);
                                    students.add(solution.getUser());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error getting solutions for task {}: {}. Skipping this task.", 
                                task.getTaskId(), e.getMessage());
                    }
                }
            }
            
            logger.debug("Found {} unique students from solutions for course {}", students.size(), course.getCourseId());
            return students;
        } catch (Exception e) {
            logger.error("Error getting students from solutions for course {}: {}", 
                    course.getCourseId(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void checkAndIssueCertificates() {
        logger.info("=== АВТОМАТИЧЕСКАЯ ПРОВЕРКА СЕРТИФИКАТОВ (01:00) ===");
        checkAndIssueCertificatesInternal();
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void checkAndIssueCertificatesEvening() {
        logger.info("=== АВТОМАТИЧЕСКАЯ ПРОВЕРКА СЕРТИФИКАТОВ (18:00) ===");
        checkAndIssueCertificatesInternal();
    }

    @Scheduled(cron = "0 0 20 * * *")
    public void checkAndIssueCertificatesNight() {
        logger.info("=== АВТОМАТИЧЕСКАЯ ПРОВЕРКА СЕРТИФИКАТОВ (20:00) ===");
        checkAndIssueCertificatesInternal();
    }

    public Map<String, Object> checkAndIssueCertificatesInternal() {
        Map<String, Object> result = new HashMap<>();
        logger.info("=== НАЧАЛО ПРОВЕРКИ СЕРТИФИКАТОВ ===");
        try {
            logger.info("Starting certificate issuance check...");
            
            LocalDate today = LocalDate.now();
            logger.info("Current date: {}", today);

            List<CourseModel> allCourses = courseRepository.findAll();
            logger.info("Total courses in database: {}", allCourses.size());
            
            List<CourseModel> finishedCourses = allCourses.stream()
                    .filter(course -> course.getEndDate() != null 
                            && (course.getEndDate().isBefore(today) || course.getEndDate().isEqual(today)))
                    .toList();

            logger.info("Found {} finished courses (endDate <= {})", finishedCourses.size(), today);
            
            int certificatesIssued = 0;
            int coursesChecked = finishedCourses.size();
            List<String> issuedCertificates = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            if (finishedCourses.isEmpty()) {
                logger.info("No finished courses found. Exiting.");
                result.put("certificatesIssued", 0);
                result.put("coursesChecked", 0);
                result.put("message", "Нет завершенных курсов для проверки");
                return result;
            }

            for (CourseModel course : finishedCourses) {
                try {
                    List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());

                    List<UserModel> students = enrollments.stream()
                            .map(EnrollmentModel::getUser)
                            .distinct()
                            .toList();

                    List<UserModel> studentsFromSolutions = getStudentsFromSolutions(course);
                    
                    Set<Long> studentIds = new HashSet<>();
                    for (UserModel s : students) {
                        if (s != null && s.getUserId() != null) {
                            studentIds.add(s.getUserId());
                        }
                    }
                    
                    for (UserModel s : studentsFromSolutions) {
                        if (s != null && s.getUserId() != null && !studentIds.contains(s.getUserId())) {
                            students.add(s);
                            studentIds.add(s.getUserId());
                        }
                    }

                    logger.info("Processing course {}: {} total students ({} from enrollments, {} from solutions)", 
                            course.getTitle(), students.size(), 
                            enrollments.size(), studentsFromSolutions.size());

                    for (UserModel student : students) {
                        try {
                            if (certificateRepository.findByUserUserIdAndCourseCourseId(
                                    student.getUserId(), course.getCourseId()).isPresent()) {
                                logger.debug("Certificate already exists for user {} course {}", 
                                        student.getEmail(), course.getTitle());
                                continue;
                            }

                            boolean eligible = eligibilityService.isEligibleForCertificate(student, course);
                            logger.info("Eligibility check for user {} (ID: {}) course {} (ID: {}): {}", 
                                    student.getEmail(), student.getUserId(), course.getTitle(), course.getCourseId(), 
                                    eligible ? "ELIGIBLE" : "NOT ELIGIBLE");
                            
                            if (eligible) {
                                logger.info("User {} is eligible for certificate for course {}", 
                                        student.getEmail(), course.getTitle());

                                CertificateModel certificate = generationService.generateCertificate(student, course);

                                certificate = certificateRepository.save(certificate);

                                boolean emailSent = emailService.sendCertificateByEmail(certificate);
                                if (emailSent) {
                                    certificateRepository.save(certificate);
                                    logger.info("Certificate issued and sent to {} for course {}", 
                                            student.getEmail(), course.getTitle());
                                } else {
                                    logger.warn("Certificate generated but email failed for {} course {}", 
                                            student.getEmail(), course.getTitle());
                                }

                                certificatesIssued++;
                                issuedCertificates.add(String.format("%s - %s", student.getEmail(), course.getTitle()));
                            } else {
                                logger.warn("User {} is NOT eligible for certificate for course {}. Check logs above for details.", 
                                        student.getEmail(), course.getTitle());
                            }
                        } catch (Exception e) {
                            String errorMsg = String.format("Ошибка для студента %s, курс %s: %s", 
                                    student.getEmail(), course.getTitle(), e.getMessage());
                            logger.error("Error processing certificate for user {} course {}: {}", 
                                    student.getEmail(), course.getTitle(), e.getMessage(), e);
                            errors.add(errorMsg);
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = String.format("Ошибка при обработке курса %s: %s", course.getTitle(), e.getMessage());
                    logger.error("Error processing course {}: {}", course.getTitle(), e.getMessage(), e);
                    errors.add(errorMsg);
                }
            }

            logger.info("Certificate issuance check completed. Issued {} certificates", certificatesIssued);
            logger.info("=== ЗАВЕРШЕНИЕ ПРОВЕРКИ СЕРТИФИКАТОВ ===");
            
            result.put("certificatesIssued", certificatesIssued);
            result.put("coursesChecked", coursesChecked);
            result.put("issuedCertificates", issuedCertificates);
            result.put("errors", errors);
            result.put("message", String.format("Проверка завершена. Выдано сертификатов: %d из %d проверенных курсов", 
                    certificatesIssued, coursesChecked));
            
            return result;
        } catch (Exception e) {
            logger.error("=== ОШИБКА В ПРОВЕРКЕ СЕРТИФИКАТОВ ===");
            logger.error("Error in certificate scheduler: {}", e.getMessage(), e);
            e.printStackTrace();
            logger.error("=== КОНЕЦ ОШИБКИ ===");
            result.put("error", "Критическая ошибка: " + e.getMessage());
            result.put("certificatesIssued", 0);
            return result;
        }
    }
}

