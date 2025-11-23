package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.CategoryRepository;
import com.example.OnlineSchoolKursach.repository.CourseRepository;
import com.example.OnlineSchoolKursach.repository.CourseStatusRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseStatusRepository courseStatusRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentStatusRepository enrollmentStatusRepository;

    @Autowired
    private FileService fileService;
    
    @Autowired
    private CheckService checkService;

    public List<CourseModel> getAllCourses() {

        List<CourseModel> allCourses = courseRepository.findAll();
        for (CourseModel course : allCourses) {
            updateCourseStatusByDatesAndCapacity(course);
        }

        allCourses = courseRepository.findAll();

        List<CourseModel> courses = allCourses.stream()
                .filter(course -> {
                    String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    return !"Архивный".equals(statusName);
                })
                .collect(Collectors.toList());

        for (CourseModel course : courses) {
            List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
            int enrolledCount = (int) allEnrollments.stream()
                    .filter(e -> e.getEnrollmentStatus() != null && 
                           ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Active".equals(e.getEnrollmentStatus().getStatusName())))
                    .count();
            course.setEnrolledCount(enrolledCount);
        }
        return convertFilePathsToUrls(courses);
    }

    public List<CourseModel> getCoursesByCategory(Long categoryId) {

        List<CourseModel> allCourses = courseRepository.findByCategoryCategoryId(categoryId);
        List<CourseModel> courses = allCourses.stream()
                .filter(course -> {
                    String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    return !"Архивный".equals(statusName);
                })
                .collect(Collectors.toList());

        for (CourseModel course : courses) {
            List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
            int enrolledCount = (int) allEnrollments.stream()
                    .filter(e -> e.getEnrollmentStatus() != null && 
                           ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Active".equals(e.getEnrollmentStatus().getStatusName())))
                    .count();
            course.setEnrolledCount(enrolledCount);
        }
        return convertFilePathsToUrls(courses);
    }

    public List<CourseModel> getEnrolledCourses(UserModel user) {
        List<EnrollmentModel> enrollments = enrollmentRepository.findByUserUserId(user.getUserId());
        List<CourseModel> courses = enrollments.stream()
                .filter(e -> e.getEnrollmentStatus() != null && 
                           ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Active".equals(e.getEnrollmentStatus().getStatusName())))
                .map(EnrollmentModel::getCourse)
                .filter(course -> {

                    String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    return !"Архивный".equals(statusName);
                })
                .collect(Collectors.toList());

        for (CourseModel course : courses) {
            List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
            int enrolledCount = (int) allEnrollments.stream()
                    .filter(e -> e.getEnrollmentStatus() != null && 
                           ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Active".equals(e.getEnrollmentStatus().getStatusName())))
                    .count();
            course.setEnrolledCount(enrolledCount);
        }
        return convertFilePathsToUrls(courses);
    }

    public List<CourseModel> getAllCoursesByTeacher(UserModel teacher) {

        List<CourseModel> allCourses = courseRepository.findByTeacherUserId(teacher.getUserId());

        for (CourseModel course : allCourses) {
            updateCourseStatusByDatesAndCapacity(course);
        }

        allCourses = courseRepository.findByTeacherUserId(teacher.getUserId());

        for (CourseModel course : allCourses) {
            List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
            int enrolledCount = (int) allEnrollments.stream()
                    .filter(e -> e.getEnrollmentStatus() != null && 
                           ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Active".equals(e.getEnrollmentStatus().getStatusName())))
                    .count();
            course.setEnrolledCount(enrolledCount);
        }
        return convertFilePathsToUrls(allCourses);
    }

    public List<EnrollmentModel> getUserEnrollments(UserModel user) {
        List<EnrollmentModel> enrollments = enrollmentRepository.findByUserUserId(user.getUserId());
        System.out.println("Found " + enrollments.size() + " enrollments for user " + user.getEmail());
        enrollments.forEach(enrollment -> {
            System.out.println("Enrollment: Course ID=" + enrollment.getCourse().getCourseId() + 
                             ", Status=" + (enrollment.getEnrollmentStatus() != null ? enrollment.getEnrollmentStatus().getStatusName() : "null"));
            if (enrollment.getCourse() != null) {
                CourseModel course = enrollment.getCourse();
                convertFilePathToUrl(course);
                
                List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
                int enrolledCount = (int) allEnrollments.stream()
                        .filter(e -> e.getEnrollmentStatus() != null && 
                               ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                                "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                                "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                                "Active".equals(e.getEnrollmentStatus().getStatusName())))
                        .count();
                course.setEnrolledCount(enrolledCount);
            }
        });
        return enrollments;
    }

    public CourseModel getCourseById(Long courseId) {
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        String oldStatus = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        updateCourseStatusByDatesAndCapacity(course);

        course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        String newStatus = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        if (oldStatus != null && !oldStatus.equals(newStatus)) {
            System.out.println("Course status updated from " + oldStatus + " to " + newStatus);
        }

        List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(courseId);
        int enrolledCount = (int) allEnrollments.stream()
                .filter(e -> e.getEnrollmentStatus() != null && 
                       ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                        "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                        "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                        "Active".equals(e.getEnrollmentStatus().getStatusName())))
                .count();
        course.setEnrolledCount(enrolledCount);
        return convertFilePathToUrl(course);
    }

    public CourseModel createCourse(CourseModel course) {

        if (course.getStartDate() == null || course.getEndDate() == null) {
            throw new RuntimeException("Необходимо указать даты начала и окончания курса");
        }
        if (!course.getEndDate().isAfter(course.getStartDate())) {
            throw new RuntimeException("Дата окончания курса должна быть позже даты начала");
        }
        if (LocalDate.now().isEqual(course.getStartDate())) {
            throw new RuntimeException("Дата начала курса не должна совпадать с датой создания");
        }

        if (course.getCourseStatus() == null) {
            List<CourseStatusModel> statuses = courseStatusRepository.findAll();
            CourseStatusModel recruiting = statuses.stream()
                    .filter(s -> "Идет набор".equals(s.getStatusName()))
                    .findFirst()
                    .orElse(null);
            if (recruiting != null) {
                course.setCourseStatus(recruiting);
            }
        }
        
        return courseRepository.save(course);
    }

    public void updateCourseStatusByDatesAndCapacity(CourseModel course) {
        try {
            List<CourseStatusModel> statuses = courseStatusRepository.findAll();
            CourseStatusModel active = statuses.stream().filter(s -> "Активен".equals(s.getStatusName())).findFirst().orElse(null);
            CourseStatusModel archived = statuses.stream().filter(s -> "Архивный".equals(s.getStatusName())).findFirst().orElse(null);
            CourseStatusModel filled = statuses.stream().filter(s -> "Заполнен".equals(s.getStatusName())).findFirst().orElse(null);
            CourseStatusModel enrolling = statuses.stream().filter(s -> "Идет набор".equals(s.getStatusName())).findFirst().orElse(null);

            LocalDate today = LocalDate.now();
            int enrolledCount = 0;
            try {
                List<EnrollmentModel> byCourse = enrollmentRepository.findByCourseCourseId(course.getCourseId());
                enrolledCount = (int) byCourse.stream()
                        .filter(e -> e.getEnrollmentStatus() != null && "Активный".equals(e.getEnrollmentStatus().getStatusName()))
                        .count();
            } catch (Exception ignored) {}

            if (course.getEndDate() != null && (today.isAfter(course.getEndDate()) || today.isEqual(course.getEndDate()))) {
                if (archived != null) {
                    course.setCourseStatus(archived);
                    System.out.println("Course " + course.getCourseId() + " set to Archived (end date: " + course.getEndDate() + ")");
                }

                List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
                if (enrollments != null && !enrollments.isEmpty()) {
                    enrollmentRepository.deleteAll(enrollments);
                }
            } else if (course.getStartDate() != null && (today.isAfter(course.getStartDate()) || today.isEqual(course.getStartDate()))) {

                if (active != null) {
                    course.setCourseStatus(active);
                    System.out.println("Course " + course.getCourseId() + " set to Active (start date: " + course.getStartDate() + ", today: " + today + ")");
                }
            } else if (course.getStartDate() != null && today.isBefore(course.getStartDate())) {

                if (course.getCapacity() != null && course.getCapacity() > 0 && enrolledCount >= course.getCapacity()) {
                    if (filled != null) {
                        course.setCourseStatus(filled);
                        System.out.println("Course " + course.getCourseId() + " set to Filled (capacity: " + course.getCapacity() + ", enrolled: " + enrolledCount + ")");
                    }
                } else {
                    if (enrolling != null) {
                        course.setCourseStatus(enrolling);
                        System.out.println("Course " + course.getCourseId() + " set to Recruiting (capacity: " + course.getCapacity() + ", enrolled: " + enrolledCount + ")");
                    }
                }
            }
            courseRepository.save(course);
        } catch (Exception ignored) {}
    }

    public CourseModel updateCourse(Long courseId, CourseModel updatedCourse, UserModel teacher) {
        CourseModel existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        
        if (!existingCourse.getTeacher().getUserId().equals(teacher.getUserId())) {
            throw new RuntimeException("У вас нет прав для редактирования этого курса");
        }
        
        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setPrice(updatedCourse.getPrice());

        if (updatedCourse.getCategory() != null && updatedCourse.getCategory().getCategoryId() != null) {
            categoryRepository.findById(updatedCourse.getCategory().getCategoryId())
                    .ifPresent(existingCourse::setCategory);
        }

        if (updatedCourse.getCourseStatus() != null && updatedCourse.getCourseStatus().getCourseStatusId() != null) {
            courseStatusRepository.findById(updatedCourse.getCourseStatus().getCourseStatusId())
                    .ifPresent(existingCourse::setCourseStatus);
        }

        existingCourse.setCapacity(updatedCourse.getCapacity());
        existingCourse.setStartDate(updatedCourse.getStartDate());
        existingCourse.setEndDate(updatedCourse.getEndDate());
        
        if (updatedCourse.getImageUrl() != null) {
            existingCourse.setImageUrl(updatedCourse.getImageUrl());
        }

        CourseModel savedCourse = courseRepository.save(existingCourse);
        updateCourseStatusByDatesAndCapacity(savedCourse);

        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
    }

    public EnrollmentModel enrollUserInCourse(UserModel user, Long courseId) {
        List<EnrollmentModel> existingEnrollments = enrollmentRepository.findByUserUserId(user.getUserId());
        boolean alreadyEnrolled = existingEnrollments.stream()
                .anyMatch(e -> e.getCourse().getCourseId().equals(courseId));
        
        if (alreadyEnrolled) {
            throw new RuntimeException("Пользователь уже записан на этот курс");
        }

        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        if ("Заполнен".equals(statusName)) {
            throw new RuntimeException("Курс полностью заполнен. Запись на курс недоступна.");
        }

        if (course.getCapacity() != null && course.getCapacity() > 0) {
            List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(courseId);
            int current = (int) allEnrollments.stream()
                    .filter(e -> e.getEnrollmentStatus() != null && "Активный".equals(e.getEnrollmentStatus().getStatusName()))
                    .count();
            if (current >= course.getCapacity()) {

                checkAndUpdateCourseStatusIfFull(course);
                throw new RuntimeException("Курс полностью заполнен. Все места заняты. Запись на курс недоступна.");
            }
        }

        if (course.getStartDate() == null || !"Идет набор".equals(statusName) || !LocalDate.now().isBefore(course.getStartDate())) {
            throw new RuntimeException("Запись на курс недоступна. Курс либо уже начался, либо набор закрыт.");
        }

        Optional<EnrollmentStatusModel> statusOptional = enrollmentStatusRepository.findAll().stream()
                .filter(status -> "Ожидает подтверждения".equals(status.getStatusName()) || 
                                "Pending".equals(status.getStatusName()) ||
                                "На рассмотрении".equals(status.getStatusName()))
                .findFirst();
        
        EnrollmentStatusModel status;
        if (statusOptional.isPresent()) {
            status = statusOptional.get();
        } else {
            status = new EnrollmentStatusModel();
            status.setStatusName("Ожидает подтверждения");
            status = enrollmentStatusRepository.save(status);
        }

        EnrollmentModel enrollment = new EnrollmentModel();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setEnrollmentStatus(status);

        checkService.createCheck(user, course, course.getPrice());

        EnrollmentModel savedEnrollment = enrollmentRepository.save(enrollment);

        CourseModel updatedCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        checkAndUpdateCourseStatusIfFull(updatedCourse);
        
        return savedEnrollment;
    }

    public void checkAndUpdateCourseStatusIfFull(CourseModel course) {
        if (course == null || course.getCapacity() == null || course.getCapacity() <= 0) {
            return;
        }

        CourseModel freshCourse = courseRepository.findById(course.getCourseId()).orElse(null);
        if (freshCourse == null) {
            return;
        }

        List<EnrollmentModel> allEnrollments = enrollmentRepository.findByCourseCourseId(freshCourse.getCourseId());
        int currentEnrollments = (int) allEnrollments.stream()
                .filter(e -> e.getEnrollmentStatus() != null && "Активный".equals(e.getEnrollmentStatus().getStatusName()))
                .count();
        
        logger.debug("Checking course {}: capacity={}, enrolled={}", freshCourse.getCourseId(), freshCourse.getCapacity(), currentEnrollments);
        
        if (currentEnrollments >= freshCourse.getCapacity()) {
            List<CourseStatusModel> statuses = courseStatusRepository.findAll();
            CourseStatusModel filledStatus = statuses.stream()
                    .filter(s -> "Заполнен".equals(s.getStatusName()))
                    .findFirst()
                    .orElse(null);
            
            if (filledStatus != null) {
                boolean courseNotStarted = freshCourse.getStartDate() != null && LocalDate.now().isBefore(freshCourse.getStartDate());
                
                if (courseNotStarted) {
                    String currentStatusName = freshCourse.getCourseStatus() != null ? freshCourse.getCourseStatus().getStatusName() : null;
                    if (!"Заполнен".equals(currentStatusName)) {
                        freshCourse.setCourseStatus(filledStatus);
                        courseRepository.save(freshCourse);
                        logger.info("Course {} status changed to 'Заполнен' (capacity: {}, enrolled: {})", 
                                freshCourse.getCourseId(), freshCourse.getCapacity(), currentEnrollments);
                    }
                } else {
                    updateCourseStatusByDatesAndCapacity(freshCourse);
                }
            }
        }
    }

    private List<CourseModel> convertFilePathsToUrls(List<CourseModel> courses) {
        courses.forEach(this::convertFilePathToUrl);
        return courses;
    }

    private CourseModel convertFilePathToUrl(CourseModel course) {
        if (course.getImageUrl() != null && !course.getImageUrl().isEmpty() 
                && !course.getImageUrl().startsWith("http") 
                && !course.getImageUrl().startsWith("/v1/api/files/image")) {
            try {
                String fullUrl = fileService.getFileUrl(course.getImageUrl());
                if (fullUrl != null) {
                    course.setImageUrl(fullUrl);
                }
            } catch (Exception e) {
            }
        }
        return course;
    }
}