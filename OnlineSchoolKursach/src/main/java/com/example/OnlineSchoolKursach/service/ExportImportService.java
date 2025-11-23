package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.dto.CourseStatisticsDto;
import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExportImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportImportService.class);
    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_QUOTE = "\"";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseStatusRepository courseStatusRepository;

    public byte[] exportUsersToCsv() throws IOException {
        List<UserModel> users = userRepository.findAll();
        return generateCsv(
            List.of("ID", "Email", "Имя", "Фамилия", "Отчество", "Роль", "Дата регистрации"),
            users.stream().map(user -> List.of(
                String.valueOf(user.getUserId()),
                escapeCsv(user.getEmail()),
                escapeCsv(user.getFirstName()),
                escapeCsv(user.getLastName()),
                escapeCsv(user.getMiddleName() != null ? user.getMiddleName() : ""),
                escapeCsv(user.getRole() != null ? user.getRole().getRoleName() : ""),
                user.getRegistrationDate() != null ? user.getRegistrationDate().format(DATE_FORMATTER) : ""
            )).toList()
        );
    }

    public byte[] exportCoursesToCsv() throws IOException {
        List<CourseModel> courses = courseRepository.findAll();
        return generateCsv(
            List.of("ID", "Название", "Описание", "Цена", "Категория", "Преподаватель", "Статус", "Вместимость", "Дата начала", "Дата окончания"),
            courses.stream().map(course -> List.of(
                String.valueOf(course.getCourseId()),
                escapeCsv(course.getTitle()),
                escapeCsv(course.getDescription() != null ? course.getDescription() : ""),
                course.getPrice() != null ? course.getPrice().toString() : "0",
                escapeCsv(course.getCategory() != null ? course.getCategory().getCategoryName() : ""),
                escapeCsv(course.getTeacher() != null ? course.getTeacher().getEmail() : ""),
                escapeCsv(course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : ""),
                course.getCapacity() != null ? course.getCapacity().toString() : "",
                course.getStartDate() != null ? course.getStartDate().format(DateTimeFormatter.ISO_DATE) : "",
                course.getEndDate() != null ? course.getEndDate().format(DateTimeFormatter.ISO_DATE) : ""
            )).toList()
        );
    }

    public byte[] exportEnrollmentsToCsv() throws IOException {
        List<EnrollmentModel> enrollments = enrollmentRepository.findAll();
        return generateCsv(
            List.of("ID", "Студент", "Курс", "Статус", "Дата записи"),
            enrollments.stream().map(enrollment -> List.of(
                String.valueOf(enrollment.getEnrollmentId()),
                escapeCsv(enrollment.getUser() != null ? enrollment.getUser().getEmail() : ""),
                escapeCsv(enrollment.getCourse() != null ? enrollment.getCourse().getTitle() : ""),
                escapeCsv(enrollment.getEnrollmentStatus() != null ? enrollment.getEnrollmentStatus().getStatusName() : ""),
                enrollment.getEnrollmentDate() != null ? enrollment.getEnrollmentDate().format(DATE_FORMATTER) : ""
            )).toList()
        );
    }

    public byte[] exportCourseStatisticsToCsv() throws IOException {
        List<CourseStatisticsDto> stats = statisticsService.getAllCoursesStatistics();
        return generateCsv(
            List.of("ID курса", "Название", "Цена", "Статус", "Категория", "Преподаватель", 
                    "Студентов", "Вместимость", "Заполненность %", "Уроков", "Заданий", 
                    "Сертификатов", "Выручка", "Дата начала", "Дата окончания"),
            stats.stream().map(stat -> List.of(
                stat.getCourseId() != null ? String.valueOf(stat.getCourseId()) : "",
                escapeCsv(stat.getTitle() != null ? stat.getTitle() : ""),
                stat.getPrice() != null ? stat.getPrice().toString() : "0",
                escapeCsv(stat.getCourseStatus() != null ? stat.getCourseStatus() : ""),
                escapeCsv(stat.getCategoryName() != null ? stat.getCategoryName() : ""),
                escapeCsv(stat.getTeacherName() != null ? stat.getTeacherName() : ""),
                stat.getEnrolledStudents() != null ? String.valueOf(stat.getEnrolledStudents()) : "0",
                stat.getCapacity() != null ? String.valueOf(stat.getCapacity()) : "",
                stat.getFillPercentage() != null ? String.format("%.1f", stat.getFillPercentage()) : "0",
                stat.getLessonsCount() != null ? String.valueOf(stat.getLessonsCount()) : "0",
                stat.getTasksCount() != null ? String.valueOf(stat.getTasksCount()) : "0",
                stat.getCertificatesIssued() != null ? String.valueOf(stat.getCertificatesIssued()) : "0",
                stat.getTotalRevenue() != null ? stat.getTotalRevenue().toString() : "0",
                stat.getStartDate() != null ? stat.getStartDate().format(DateTimeFormatter.ISO_DATE) : "",
                stat.getEndDate() != null ? stat.getEndDate().format(DateTimeFormatter.ISO_DATE) : ""
            )).toList()
        );
    }

    public ImportResult importUsersFromCsv(MultipartFile file) {
        ImportResult result = new ImportResult();
        try {
            List<String[]> rows = parseCsv(file);
            if (rows.isEmpty()) {
                result.addError("Файл пуст");
                return result;
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 2) continue;

                try {
                    String email = row[1].trim();
                    if (email.isEmpty()) continue;

                    Optional<UserModel> existingUserOpt = userRepository.findByEmail(email);
                    if (existingUserOpt.isPresent()) {
                        result.addSkipped("Пользователь уже существует: " + email);
                        continue;
                    }

                    UserModel user = new UserModel();
                    user.setEmail(email);
                    if (row.length > 2) user.setFirstName(row[2].trim());
                    if (row.length > 3) user.setLastName(row[3].trim());
                    if (row.length > 4 && !row[4].trim().isEmpty()) user.setMiddleName(row[4].trim());
                    
                    if (row.length > 5 && !row[5].trim().isEmpty()) {
                        Optional<RoleModel> roleOpt = roleRepository.findByRoleName(row[5].trim());
                        if (roleOpt.isPresent()) {
                            user.setRole(roleOpt.get());
                        } else {
                            Optional<RoleModel> defaultRoleOpt = roleRepository.findByRoleName("Студент");
                            user.setRole(defaultRoleOpt.orElse(roleRepository.findAll().get(0)));
                        }
                    } else {
                        Optional<RoleModel> defaultRoleOpt = roleRepository.findByRoleName("Студент");
                        user.setRole(defaultRoleOpt.orElse(roleRepository.findAll().get(0)));
                    }

                    user.setRegistrationDate(LocalDate.now());
                    user.setPasswordHash("");

                    userRepository.save(user);
                    result.addSuccess("Пользователь создан: " + email);
                } catch (Exception e) {
                    result.addError("Ошибка при импорте строки " + (i + 1) + ": " + e.getMessage());
                    logger.error("Error importing user from CSV row {}: {}", i + 1, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            result.addError("Ошибка чтения файла: " + e.getMessage());
            logger.error("Error importing users from CSV: {}", e.getMessage(), e);
        }
        return result;
    }

    public ImportResult importCoursesFromCsv(MultipartFile file) {
        ImportResult result = new ImportResult();
        try {
            List<String[]> rows = parseCsv(file);
            if (rows.isEmpty()) {
                result.addError("Файл пуст");
                return result;
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 2) continue;

                try {
                    String title = row[1].trim();
                    if (title.isEmpty()) continue;

                    CourseModel course = null;
                    boolean isUpdate = false;
                    if (row.length > 0 && !row[0].trim().isEmpty()) {
                        try {
                            Long courseId = Long.parseLong(row[0].trim());
                            Optional<CourseModel> existingCourseOpt = courseRepository.findById(courseId);
                            if (existingCourseOpt.isPresent()) {
                                course = existingCourseOpt.get();
                                isUpdate = true;
                                logger.info("Обновление существующего курса ID: {}", courseId);
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                    
                    if (course == null) {
                        course = new CourseModel();
                        isUpdate = false;
                    }
                    
                    course.setTitle(title);
                    if (row.length > 2) course.setDescription(row[2].trim());
                    if (row.length > 3 && !row[3].trim().isEmpty()) {
                        try {
                            course.setPrice(new java.math.BigDecimal(row[3].trim()));
                        } catch (NumberFormatException e) {
                            course.setPrice(java.math.BigDecimal.ZERO);
                        }
                    }

                    if (row.length > 4 && !row[4].trim().isEmpty()) {
                        Optional<CategoryModel> categoryOpt = categoryRepository.findByCategoryName(row[4].trim());
                        if (categoryOpt.isPresent()) {
                            course.setCategory(categoryOpt.get());
                        }
                    }

                    if (row.length > 5 && !row[5].trim().isEmpty()) {
                        String teacherEmail = row[5].trim();
                        Optional<UserModel> teacherOpt = userRepository.findByEmail(teacherEmail);
                        if (teacherOpt.isPresent()) {
                            UserModel teacher = teacherOpt.get();
                            if (teacher.getRole() != null && "Преподаватель".equals(teacher.getRole().getRoleName())) {
                                course.setTeacher(teacher);
                            } else {
                                result.addError("Пользователь " + teacherEmail + " не является преподавателем");
                                if (!isUpdate) {
                                    continue;
                                }
                            }
                        } else {
                            result.addError("Преподаватель не найден: " + teacherEmail);
                            if (!isUpdate) {
                                continue;
                            }
                        }
                    } else {
                        if (!isUpdate && course.getTeacher() == null) {
                            result.addError("Для нового курса необходимо указать преподавателя (email)");
                            continue;
                        }
                    }

                    if (row.length > 6 && !row[6].trim().isEmpty()) {
                        String statusName = row[6].trim();
                        Optional<CourseStatusModel> statusOpt = courseStatusRepository.findAll().stream()
                                .filter(s -> s.getStatusName().equalsIgnoreCase(statusName))
                                .findFirst();
                        if (statusOpt.isPresent()) {
                            course.setCourseStatus(statusOpt.get());
                        } else {
                            setDefaultCourseStatus(course);
                        }
                    } else {
                        setDefaultCourseStatus(course);
                    }

                    if (row.length > 7 && !row[7].trim().isEmpty()) {
                        try {
                            course.setCapacity(Integer.parseInt(row[7].trim()));
                        } catch (NumberFormatException e) {
                            course.setCapacity(null);
                        }
                    }

                    if (row.length > 8 && !row[8].trim().isEmpty()) {
                        try {
                            course.setStartDate(LocalDate.parse(row[8].trim(), DateTimeFormatter.ISO_DATE));
                        } catch (Exception e) {
                            logger.warn("Не удалось распарсить дату начала для курса {}: {}", title, row[8]);
                        }
                    }

                    if (row.length > 9 && !row[9].trim().isEmpty()) {
                        try {
                            course.setEndDate(LocalDate.parse(row[9].trim(), DateTimeFormatter.ISO_DATE));
                        } catch (Exception e) {
                            logger.warn("Не удалось распарсить дату окончания для курса {}: {}", title, row[9]);
                        }
                    }

                    // Финальная проверка обязательных полей
                    if (course.getTeacher() == null) {
                        result.addError("Курс '" + title + "' не может быть сохранен: не указан преподаватель");
                        continue;
                    }
                    
                    if (course.getCourseStatus() == null) {
                        setDefaultCourseStatus(course);
                    }

                    try {
                    courseRepository.save(course);
                    if (isUpdate) {
                        result.addSuccess("Курс обновлен: " + title + " (ID: " + course.getCourseId() + ")");
                    } else {
                        result.addSuccess("Курс создан: " + title + " (ID: " + course.getCourseId() + ")");
                        }
                    } catch (Exception e) {
                        result.addError("Ошибка сохранения курса '" + title + "': " + e.getMessage());
                        logger.error("Error saving course '{}': {}", title, e.getMessage(), e);
                    }
                } catch (Exception e) {
                    result.addError("Ошибка при импорте строки " + (i + 1) + ": " + e.getMessage());
                    logger.error("Error importing course from CSV row {}: {}", i + 1, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            result.addError("Ошибка чтения файла: " + e.getMessage());
            logger.error("Error importing courses from CSV: {}", e.getMessage(), e);
        }
        return result;
    }

    private byte[] generateCsv(List<String> headers, List<List<String>> rows) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            writer.write(String.join(CSV_SEPARATOR, headers.stream()
                .map(this::escapeCsv)
                .toList()));
            writer.newLine();

            for (List<String> row : rows) {
                writer.write(String.join(CSV_SEPARATOR, row));
                writer.newLine();
            }
        }
        return baos.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(CSV_SEPARATOR) || value.contains("\n") || value.contains(CSV_QUOTE)) {
            return CSV_QUOTE + value.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE) + CSV_QUOTE;
        }
        return value;
    }

    private List<String[]> parseCsv(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(CSV_SEPARATOR);
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim().replace(CSV_QUOTE + CSV_QUOTE, CSV_QUOTE);
                    if (values[i].startsWith(CSV_QUOTE) && values[i].endsWith(CSV_QUOTE)) {
                        values[i] = values[i].substring(1, values[i].length() - 1);
                    }
                }
                rows.add(values);
            }
        }
        return rows;
    }

    @Autowired
    private StatisticsService statisticsService;

    private void setDefaultCourseStatus(CourseModel course) {
        Optional<CourseStatusModel> defaultStatus = courseStatusRepository.findAll().stream()
                .filter(s -> "Набор".equals(s.getStatusName()) || "Активный".equals(s.getStatusName()))
                .findFirst();
        
        if (defaultStatus.isPresent()) {
            course.setCourseStatus(defaultStatus.get());
        } else {
            List<CourseStatusModel> allStatuses = courseStatusRepository.findAll();
            if (!allStatuses.isEmpty()) {
                course.setCourseStatus(allStatuses.get(0));
            } else {
                CourseStatusModel newStatus = new CourseStatusModel("Набор");
                course.setCourseStatus(courseStatusRepository.save(newStatus));
            }
        }
    }

    public static class ImportResult {
        private final List<String> successes = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> skipped = new ArrayList<>();

        public void addSuccess(String message) {
            successes.add(message);
        }

        public void addError(String message) {
            errors.add(message);
        }

        public void addSkipped(String message) {
            skipped.add(message);
        }

        public List<String> getSuccesses() {
            return successes;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getSkipped() {
            return skipped;
        }

        public int getTotalProcessed() {
            return successes.size() + errors.size() + skipped.size();
        }
    }
}

