package com.example.OnlineSchoolKursach.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CourseStatisticsDto {
    private Long courseId;
    private String title;
    private BigDecimal price;
    private String courseStatus;
    private String categoryName;
    private String teacherName;
    private Integer enrolledStudents;
    private Integer capacity;
    private BigDecimal fillPercentage;
    private Integer lessonsCount;
    private Integer tasksCount;
    private Integer certificatesIssued;
    private BigDecimal totalRevenue;
    private LocalDate startDate;
    private LocalDate endDate;

    public CourseStatisticsDto() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCourseStatus() { return courseStatus; }
    public void setCourseStatus(String courseStatus) { this.courseStatus = courseStatus; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(Integer enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public BigDecimal getFillPercentage() { return fillPercentage; }
    public void setFillPercentage(BigDecimal fillPercentage) { this.fillPercentage = fillPercentage; }

    public Integer getLessonsCount() { return lessonsCount; }
    public void setLessonsCount(Integer lessonsCount) { this.lessonsCount = lessonsCount; }

    public Integer getTasksCount() { return tasksCount; }
    public void setTasksCount(Integer tasksCount) { this.tasksCount = tasksCount; }

    public Integer getCertificatesIssued() { return certificatesIssued; }
    public void setCertificatesIssued(Integer certificatesIssued) { this.certificatesIssued = certificatesIssued; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}

