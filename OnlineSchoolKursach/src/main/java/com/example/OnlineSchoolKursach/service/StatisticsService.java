package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.dto.AuditLogDto;
import com.example.OnlineSchoolKursach.dto.CourseStatisticsDto;
import com.example.OnlineSchoolKursach.repository.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    public CourseStatisticsDto getCourseStatistics(Long courseId) {
        Query query = entityManager.createNativeQuery(
            "SELECT course_id, title, price, course_status, category_name, teacher_name, " +
            "enrolled_students, capacity, fill_percentage, lessons_count, tasks_count, " +
            "certificates_issued, total_revenue, start_date, end_date " +
            "FROM v_course_statistics WHERE course_id = :courseId"
        );
        query.setParameter("courseId", courseId);
        try {
            Object[] result = (Object[]) query.getSingleResult();
            return mapToCourseStatisticsDto(result);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<CourseStatisticsDto> getAllCoursesStatistics() {
        Query query = entityManager.createNativeQuery(
            "SELECT course_id, title, price, course_status, category_name, teacher_name, " +
            "enrolled_students, capacity, fill_percentage, lessons_count, tasks_count, " +
            "certificates_issued, total_revenue, start_date, end_date " +
            "FROM v_course_statistics ORDER BY total_revenue DESC"
        );
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<CourseStatisticsDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            dtos.add(mapToCourseStatisticsDto(row));
        }
        return dtos;
    }
    
    private CourseStatisticsDto mapToCourseStatisticsDto(Object[] row) {
        CourseStatisticsDto dto = new CourseStatisticsDto();
        try {
            if (row.length > 0 && row[0] != null) dto.setCourseId(((Number) row[0]).longValue());
            if (row.length > 1) dto.setTitle((String) row[1]);
            if (row.length > 2) dto.setPrice(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO);
            if (row.length > 3) dto.setCourseStatus((String) row[3]);
            if (row.length > 4) dto.setCategoryName((String) row[4]);
            if (row.length > 5) dto.setTeacherName((String) row[5]);
            if (row.length > 6) dto.setEnrolledStudents(row[6] != null ? ((Number) row[6]).intValue() : 0);
            if (row.length > 7) dto.setCapacity(row[7] != null ? ((Number) row[7]).intValue() : null);
            if (row.length > 8) dto.setFillPercentage(row[8] != null ? (BigDecimal) row[8] : null);
            if (row.length > 9) dto.setLessonsCount(row[9] != null ? ((Number) row[9]).intValue() : 0);
            if (row.length > 10) dto.setTasksCount(row[10] != null ? ((Number) row[10]).intValue() : 0);
            if (row.length > 11) dto.setCertificatesIssued(row[11] != null ? ((Number) row[11]).intValue() : 0);
            if (row.length > 12) dto.setTotalRevenue(row[12] != null ? (BigDecimal) row[12] : BigDecimal.ZERO);
            if (row.length > 13 && row[13] != null) {
                if (row[13] instanceof java.sql.Date) {
                    dto.setStartDate(((java.sql.Date) row[13]).toLocalDate());
                } else if (row[13] instanceof java.time.LocalDate) {
                    dto.setStartDate((java.time.LocalDate) row[13]);
                }
            }
            if (row.length > 14 && row[14] != null) {
                if (row[14] instanceof java.sql.Date) {
                    dto.setEndDate(((java.sql.Date) row[14]).toLocalDate());
                } else if (row[14] instanceof java.time.LocalDate) {
                    dto.setEndDate((java.time.LocalDate) row[14]);
                }
            }
        } catch (Exception e) {
        }
        return dto;
    }
    
    public BigDecimal getCourseRevenue(Long courseId) {
        try {
            BigDecimal revenue = courseRepository.calculateCourseRevenue(courseId);
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    public List<AuditLogDto> getCourseHistory(Long courseId) {
        Query query = entityManager.createNativeQuery(
            "SELECT al.audit_id, al.table_name, al.record_id, al.action, al.user_id, " +
            "al.changed_at, al.old_values::text as old_values, al.new_values::text as new_values, " +
            "COALESCE(u.first_name || ' ' || u.last_name, 'Система') as user_name " +
            "FROM audit_log al " +
            "LEFT JOIN users u ON al.user_id = u.user_id " +
            "WHERE al.table_name = :tableName AND al.record_id = :recordId " +
            "ORDER BY al.changed_at DESC"
        );
        query.setParameter("tableName", "courses");
        query.setParameter("recordId", courseId);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapToAuditLogDtoList(results);
    }
    
    public List<AuditLogDto> getUserHistory(Long userId, int limit) {
        Query query = entityManager.createNativeQuery(
            "SELECT al.audit_id, al.table_name, al.record_id, al.action, al.user_id, " +
            "al.changed_at, al.old_values::text as old_values, al.new_values::text as new_values, " +
            "COALESCE(u.first_name || ' ' || u.last_name, 'Система') as user_name " +
            "FROM audit_log al " +
            "LEFT JOIN users u ON al.user_id = u.user_id " +
            "WHERE al.user_id = :userId " +
            "ORDER BY al.changed_at DESC LIMIT :limit"
        );
        query.setParameter("userId", userId);
        query.setParameter("limit", limit);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapToAuditLogDtoList(results);
    }
    
    public List<AuditLogDto> getRecentHistory(int days, int limit) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        Query query = entityManager.createNativeQuery(
            "SELECT al.audit_id, al.table_name, al.record_id, al.action, al.user_id, " +
            "al.changed_at, al.old_values::text as old_values, al.new_values::text as new_values, " +
            "COALESCE(u.first_name || ' ' || u.last_name, 'Система') as user_name " +
            "FROM audit_log al " +
            "LEFT JOIN users u ON al.user_id = u.user_id " +
            "WHERE al.changed_at >= :fromDate " +
            "ORDER BY al.changed_at DESC LIMIT :limit"
        );
        query.setParameter("fromDate", fromDate);
        query.setParameter("limit", limit);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapToAuditLogDtoList(results);
    }
    
    public List<AuditLogDto> getAllRecentHistory(int limit) {
        Query query = entityManager.createNativeQuery(
            "SELECT al.audit_id, al.table_name, al.record_id, al.action, al.user_id, " +
            "al.changed_at, al.old_values::text as old_values, al.new_values::text as new_values, " +
            "COALESCE(u.first_name || ' ' || u.last_name, 'Система') as user_name " +
            "FROM audit_log al " +
            "LEFT JOIN users u ON al.user_id = u.user_id " +
            "ORDER BY al.changed_at DESC LIMIT :limit"
        );
        query.setParameter("limit", limit);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapToAuditLogDtoList(results);
    }
    
    private List<AuditLogDto> mapToAuditLogDtoList(List<Object[]> results) {
        List<AuditLogDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            AuditLogDto dto = new AuditLogDto();
            if (row.length > 0) dto.setAuditId(row[0] != null ? ((Number) row[0]).longValue() : null);
            if (row.length > 1) dto.setTableName((String) row[1]);
            if (row.length > 2) dto.setRecordId(row[2] != null ? ((Number) row[2]).longValue() : null);
            if (row.length > 3) dto.setAction((String) row[3]);
            if (row.length > 4) dto.setUserId(row[4] != null ? ((Number) row[4]).longValue() : null);
            if (row.length > 5 && row[5] != null) {
                if (row[5] instanceof java.sql.Timestamp) {
                    dto.setChangedAt(((java.sql.Timestamp) row[5]).toLocalDateTime());
                } else if (row[5] instanceof LocalDateTime) {
                    dto.setChangedAt((LocalDateTime) row[5]);
                }
            }
            if (row.length > 6) dto.setOldValues((String) row[6]);
            if (row.length > 7) dto.setNewValues((String) row[7]);
            if (row.length > 8) dto.setUserName((String) row[8]);
            dtos.add(dto);
        }
        return dtos;
    }
    
    public Map<String, Object> getDashboardStats(String dateFrom, String dateTo) {
        Map<String, Object> stats = new HashMap<>();
        
        List<CourseStatisticsDto> allStats = getAllCoursesStatistics();

        if (dateFrom != null && !dateFrom.isEmpty() || dateTo != null && !dateTo.isEmpty()) {
            allStats = allStats.stream()
                .filter(s -> {
                    boolean matches = true;
                    if (dateFrom != null && !dateFrom.isEmpty() && s.getStartDate() != null) {
                        matches = matches && !s.getStartDate().isBefore(java.time.LocalDate.parse(dateFrom));
                    }
                    if (dateTo != null && !dateTo.isEmpty() && s.getStartDate() != null) {
                        matches = matches && !s.getStartDate().isAfter(java.time.LocalDate.parse(dateTo));
                    }
                    return matches;
                })
                .toList();
        }

        stats.put("totalCourses", allStats.size());
        stats.put("totalStudents", allStats.stream()
            .mapToInt(s -> s.getEnrolledStudents() != null ? s.getEnrolledStudents() : 0)
            .sum());
        stats.put("totalRevenue", allStats.stream()
            .map(s -> s.getTotalRevenue() != null ? s.getTotalRevenue() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.put("totalCertificates", allStats.stream()
            .mapToInt(s -> s.getCertificatesIssued() != null ? s.getCertificatesIssued() : 0)
            .sum());

        Map<String, Long> statusCounts = new HashMap<>();
        allStats.forEach(s -> {
            String status = s.getCourseStatus() != null ? s.getCourseStatus() : "Неизвестно";
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        });
        stats.put("statusCounts", statusCounts);

        List<CourseStatisticsDto> topCourses = allStats.stream()
            .sorted((a, b) -> {
                BigDecimal revA = a.getTotalRevenue() != null ? a.getTotalRevenue() : BigDecimal.ZERO;
                BigDecimal revB = b.getTotalRevenue() != null ? b.getTotalRevenue() : BigDecimal.ZERO;
                return revB.compareTo(revA);
            })
            .limit(5)
            .toList();
        stats.put("topCourses", topCourses);
        
        return stats;
    }
}

