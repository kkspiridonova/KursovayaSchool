package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.CategoryRepository;
import com.example.OnlineSchoolKursach.repository.CourseRepository;
import com.example.OnlineSchoolKursach.repository.CourseStatusRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

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
        // Update statuses based on dates before filtering
        List<CourseModel> allCourses = courseRepository.findAll();
        for (CourseModel course : allCourses) {
            updateCourseStatusByDatesAndCapacity(course);
        }
        // Reload courses to get updated statuses
        allCourses = courseRepository.findAll();
        // Exclude archived courses from catalog
        List<CourseModel> courses = allCourses.stream()
                .filter(course -> {
                    String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    return !"Архивный".equals(statusName);
                })
                .collect(Collectors.toList());
        // Set enrolled count for each course
        for (CourseModel course : courses) {
            int enrolledCount = enrollmentRepository.findByCourseCourseId(course.getCourseId()).size();
            course.setEnrolledCount(enrolledCount);
        }
        return convertFilePathsToUrls(courses);
    }

    public List<CourseModel> getCoursesByCategory(Long categoryId) {
        // Exclude archived courses from catalog
        List<CourseModel> allCourses = courseRepository.findByCategoryCategoryId(categoryId);
        List<CourseModel> courses = allCourses.stream()
                .filter(course -> {
                    String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    return !"Архивный".equals(statusName);
                })
                .collect(Collectors.toList());
        // Set enrolled count for each course
        for (CourseModel course : courses) {
            int enrolledCount = enrollmentRepository.findByCourseCourseId(course.getCourseId()).size();
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
                    // Exclude archived courses
                    String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    return !"Архивный".equals(statusName);
                })
                .collect(Collectors.toList());
        // Set enrolled count for each course
        for (CourseModel course : courses) {
            int enrolledCount = enrollmentRepository.findByCourseCourseId(course.getCourseId()).size();
            course.setEnrolledCount(enrolledCount);
        }
        return convertFilePathsToUrls(courses);
    }

    public List<CourseModel> getAllCoursesByTeacher(UserModel teacher) {
        // Get all courses by teacher (including archived)
        List<CourseModel> allCourses = courseRepository.findByTeacherUserId(teacher.getUserId());
        // Update statuses based on dates
        for (CourseModel course : allCourses) {
            updateCourseStatusByDatesAndCapacity(course);
        }
        // Reload courses to get updated statuses
        allCourses = courseRepository.findByTeacherUserId(teacher.getUserId());
        // Set enrolled count for each course
        for (CourseModel course : allCourses) {
            int enrolledCount = enrollmentRepository.findByCourseCourseId(course.getCourseId()).size();
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
            }
        });
        return enrollments;
    }

    public CourseModel getCourseById(Long courseId) {
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        // Update status based on dates before returning
        String oldStatus = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        updateCourseStatusByDatesAndCapacity(course);
        // Reload course to get updated status
        course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        String newStatus = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        if (oldStatus != null && !oldStatus.equals(newStatus)) {
            System.out.println("Course status updated from " + oldStatus + " to " + newStatus);
        }
        // Set enrolled count
        int enrolledCount = enrollmentRepository.findByCourseCourseId(courseId).size();
        course.setEnrolledCount(enrolledCount);
        return convertFilePathToUrl(course);
    }

    public CourseModel createCourse(CourseModel course) {
        // Validate dates
        if (course.getStartDate() == null || course.getEndDate() == null) {
            throw new RuntimeException("Необходимо указать даты начала и окончания курса");
        }
        if (!course.getEndDate().isAfter(course.getStartDate())) {
            throw new RuntimeException("Дата окончания курса должна быть позже даты начала");
        }
        if (LocalDate.now().isEqual(course.getStartDate())) {
            throw new RuntimeException("Дата начала курса не должна совпадать с датой создания");
        }

        // Default status: "Идет набор"
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
            CourseStatusModel active = statuses.stream().filter(s -> "Активный".equals(s.getStatusName())).findFirst().orElse(null);
            CourseStatusModel archived = statuses.stream().filter(s -> "Архивный".equals(s.getStatusName())).findFirst().orElse(null);
            CourseStatusModel filled = statuses.stream().filter(s -> "Заполнен".equals(s.getStatusName())).findFirst().orElse(null);
            CourseStatusModel enrolling = statuses.stream().filter(s -> "Идет набор".equals(s.getStatusName())).findFirst().orElse(null);

            LocalDate today = LocalDate.now();
            int enrolledCount = 0;
            try {
                List<EnrollmentModel> byCourse = enrollmentRepository.findByCourseCourseId(course.getCourseId());
                enrolledCount = (int) byCourse.stream().filter(e -> e.getEnrollmentStatus() != null && "Активен".equals(e.getEnrollmentStatus().getStatusName())).count();
            } catch (Exception ignored) {}

            // Check end date first - if course ended, set to archived
            if (course.getEndDate() != null && (today.isAfter(course.getEndDate()) || today.isEqual(course.getEndDate()))) {
                if (archived != null) {
                    course.setCourseStatus(archived);
                    System.out.println("Course " + course.getCourseId() + " set to Archived (end date: " + course.getEndDate() + ")");
                }
                // logically remove enrollments when course ended
                List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
                if (enrollments != null && !enrollments.isEmpty()) {
                    enrollmentRepository.deleteAll(enrollments);
                }
            } else if (course.getStartDate() != null && (today.isAfter(course.getStartDate()) || today.isEqual(course.getStartDate()))) {
                // Course has started - set to Active
                if (active != null) {
                    course.setCourseStatus(active);
                    System.out.println("Course " + course.getCourseId() + " set to Active (start date: " + course.getStartDate() + ", today: " + today + ")");
                }
            } else if (course.getStartDate() != null && today.isBefore(course.getStartDate())) {
                // Before start - check capacity
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
        
        // Load category from repository if provided
        if (updatedCourse.getCategory() != null && updatedCourse.getCategory().getCategoryId() != null) {
            categoryRepository.findById(updatedCourse.getCategory().getCategoryId())
                    .ifPresent(existingCourse::setCategory);
        }
        
        // Load course status from repository if provided
        if (updatedCourse.getCourseStatus() != null && updatedCourse.getCourseStatus().getCourseStatusId() != null) {
            courseStatusRepository.findById(updatedCourse.getCourseStatus().getCourseStatusId())
                    .ifPresent(existingCourse::setCourseStatus);
        }
        
        // Update capacity, startDate, and endDate
        existingCourse.setCapacity(updatedCourse.getCapacity());
        existingCourse.setStartDate(updatedCourse.getStartDate());
        existingCourse.setEndDate(updatedCourse.getEndDate());
        
        if (updatedCourse.getImageUrl() != null) {
            existingCourse.setImageUrl(updatedCourse.getImageUrl());
        }
        
        // After updating dates, check if status needs to be updated
        CourseModel savedCourse = courseRepository.save(existingCourse);
        updateCourseStatusByDatesAndCapacity(savedCourse);
        
        // Reload to get updated status
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

        // Check course status first
        String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        if ("Заполнен".equals(statusName)) {
            throw new RuntimeException("Курс полностью заполнен. Запись на курс недоступна.");
        }
        
        // Check capacity before checking status
        if (course.getCapacity() != null && course.getCapacity() > 0) {
            int current = enrollmentRepository.findByCourseCourseId(courseId).size();
            if (current >= course.getCapacity()) {
                // Update status to "Заполнен" if not already set
                checkAndUpdateCourseStatusIfFull(course);
                throw new RuntimeException("Курс полностью заполнен. Все места заняты. Запись на курс недоступна.");
            }
        }

        // Allow enrollments only while recruiting and strictly before start date
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
        enrollment.setDate(LocalDate.now());
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setEnrollmentStatus(status);

        // Create a receipt for the enrollment
        checkService.createCheck(user, course, course.getPrice());

        EnrollmentModel savedEnrollment = enrollmentRepository.save(enrollment);
        
        // Reload course from database to get fresh data
        CourseModel updatedCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        
        // Check if course is now full and update status
        checkAndUpdateCourseStatusIfFull(updatedCourse);
        
        return savedEnrollment;
    }

    /**
     * Проверяет, достигнуто ли максимальное количество студентов на курсе,
     * и если да - обновляет статус курса на "Заполнен"
     */
    public void checkAndUpdateCourseStatusIfFull(CourseModel course) {
        if (course == null || course.getCapacity() == null || course.getCapacity() <= 0) {
            return;
        }
        
        // Count all enrollments for this course
        int currentEnrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId()).size();
        
        if (currentEnrollments >= course.getCapacity()) {
            // Course is now full, update status to "Заполнен"
            List<CourseStatusModel> statuses = courseStatusRepository.findAll();
            CourseStatusModel filledStatus = statuses.stream()
                    .filter(s -> "Заполнен".equals(s.getStatusName()))
                    .findFirst()
                    .orElse(null);
            
            if (filledStatus != null) {
                // Check if course hasn't started yet
                boolean courseNotStarted = course.getStartDate() != null && LocalDate.now().isBefore(course.getStartDate());
                
                if (courseNotStarted) {
                    // Only set to "Заполнен" if course hasn't started yet
                    String currentStatusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
                    if (!"Заполнен".equals(currentStatusName)) {
                        course.setCourseStatus(filledStatus);
                        courseRepository.save(course);
                    }
                } else {
                    // If course has started, status will be managed by updateCourseStatusByDatesAndCapacity
                    // But we still want to mark it as full if capacity is reached
                    updateCourseStatusByDatesAndCapacity(course);
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
                // Log error but don't fail
            }
        }
        return course;
    }
}