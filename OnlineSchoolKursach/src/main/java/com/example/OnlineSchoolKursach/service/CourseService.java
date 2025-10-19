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

    public List<CourseModel> getAllCourses() {
        List<CourseModel> courses = courseRepository.findAll();
        return convertFilePathsToUrls(courses);
    }

    public List<CourseModel> getCoursesByCategory(Long categoryId) {
        List<CourseModel> courses = courseRepository.findByCategoryCategoryId(categoryId);
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
                .collect(Collectors.toList());
        return convertFilePathsToUrls(courses);
    }

    public List<EnrollmentModel> getUserEnrollments(UserModel user) {
        List<EnrollmentModel> enrollments = enrollmentRepository.findByUserUserId(user.getUserId());
        enrollments.forEach(enrollment -> {
            if (enrollment.getCourse() != null) {
                CourseModel course = enrollment.getCourse();
                if (course.getImageUrl() != null && !course.getImageUrl().startsWith("http")) {
                    course.setImageUrl(fileService.getFileUrl(course.getImageUrl()));
                }
            }
        });
        return enrollments;
    }

    public CourseModel getCourseById(Long courseId) {
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        return convertFilePathToUrl(course);
    }

    public CourseModel createCourse(CourseModel course) {
        if (course.getCourseStatus() == null) {
            List<CourseStatusModel> statuses = courseStatusRepository.findAll();
            if (!statuses.isEmpty()) {
                course.setCourseStatus(statuses.get(0));
            }
        }
        
        return courseRepository.save(course);
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
        existingCourse.setCategory(updatedCourse.getCategory());
        existingCourse.setCourseStatus(updatedCourse.getCourseStatus());
        if (updatedCourse.getImageUrl() != null) {
            existingCourse.setImageUrl(updatedCourse.getImageUrl());
        }
        
        return courseRepository.save(existingCourse);
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

        return enrollmentRepository.save(enrollment);
    }

    private List<CourseModel> convertFilePathsToUrls(List<CourseModel> courses) {
        courses.forEach(this::convertFilePathToUrl);
        return courses;
    }

    private CourseModel convertFilePathToUrl(CourseModel course) {
        if (course.getImageUrl() != null && !course.getImageUrl().startsWith("http")) {
            try {
                String fullUrl = fileService.getFileUrl(course.getImageUrl());
                course.setImageUrl(fullUrl);
            } catch (Exception e) {
            }
        }
        return course;
    }
}