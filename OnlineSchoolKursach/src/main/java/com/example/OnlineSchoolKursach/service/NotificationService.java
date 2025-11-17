package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.*;
import com.example.OnlineSchoolKursach.repository.EnrollmentRepository;
import com.example.OnlineSchoolKursach.repository.SolutionRepository;
import com.example.OnlineSchoolKursach.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public void sendGradeNotification(GradeModel grade, SolutionModel solution) {
        try {
            if (solution == null || solution.getUser() == null) {
                logger.warn("Cannot send grade notification: solution or user is null");
                return;
            }

            UserModel student = solution.getUser();
            TaskModel task = solution.getTask();
            if (task == null) {
                logger.warn("Cannot send grade notification: task is null");
                return;
            }

            String studentEmail = student.getEmail();
            String studentName = buildStudentName(student);
            String taskTitle = task.getTitle();
            String courseTitle = task.getLesson() != null && task.getLesson().getCourse() != null 
                ? task.getLesson().getCourse().getTitle() 
                : "Неизвестный курс";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(studentEmail);
            helper.setSubject("Оценка выставлена: " + taskTitle);

            String emailBody = buildGradeNotificationBody(studentName, courseTitle, taskTitle, 
                    grade.getGradeValue(), grade.getFeedback());

            helper.setText(emailBody, true);

            mailSender.send(message);

            logger.info("Grade notification sent successfully to: {} for task: {}", 
                    studentEmail, taskTitle);

        } catch (MessagingException e) {
            logger.error("Error sending grade notification: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error sending grade notification: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineReminders() {
        logger.info("=== НАЧАЛО ПРОВЕРКИ ДЕДЛАЙНОВ ===");
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            logger.info("Checking tasks with deadline: {}", tomorrow);

            List<TaskModel> tasks = taskRepository.findAll();
            List<TaskModel> tasksDueTomorrow = tasks.stream()
                    .filter(task -> task.getDeadline() != null && task.getDeadline().equals(tomorrow))
                    .toList();

            logger.info("Found {} tasks with deadline tomorrow", tasksDueTomorrow.size());

            int notificationsSent = 0;

            for (TaskModel task : tasksDueTomorrow) {
                try {
                    if (task.getLesson() == null || task.getLesson().getCourse() == null) {
                        continue;
                    }

                    CourseModel course = task.getLesson().getCourse();
                    String courseTitle = course.getTitle();
                    String taskTitle = task.getTitle();

                    List<EnrollmentModel> enrollments = enrollmentRepository.findByCourseCourseId(course.getCourseId());
                    List<UserModel> students = enrollments.stream()
                            .filter(enrollment -> enrollment.getEnrollmentStatus() != null 
                                    && "Активен".equals(enrollment.getEnrollmentStatus().getStatusName()))
                            .map(EnrollmentModel::getUser)
                            .distinct()
                            .toList();

                    logger.info("Found {} active students for course {}", students.size(), courseTitle);

                    for (UserModel student : students) {
                        try {
                            SolutionModel existingSolution = solutionRepository
                                    .findFirstByTaskTaskIdAndUserUserId(task.getTaskId(), student.getUserId());
                            
                            if (existingSolution != null) {
                                logger.debug("Student {} already submitted solution for task {}, skipping notification", 
                                        student.getEmail(), taskTitle);
                                continue;
                            }

                            sendDeadlineReminder(student, courseTitle, taskTitle, task.getDeadline());
                            notificationsSent++;

                        } catch (Exception e) {
                            logger.error("Error sending deadline reminder to student {} for task {}: {}", 
                                    student.getEmail(), taskTitle, e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing task {}: {}", task.getTaskId(), e.getMessage(), e);
                }
            }

            logger.info("Deadline reminder check completed. Sent {} notifications", notificationsSent);
            logger.info("=== ЗАВЕРШЕНИЕ ПРОВЕРКИ ДЕДЛАЙНОВ ===");

        } catch (Exception e) {
            logger.error("=== ОШИБКА В ПРОВЕРКЕ ДЕДЛАЙНОВ ===");
            logger.error("Error in deadline reminder scheduler: {}", e.getMessage(), e);
        }
    }

    private void sendDeadlineReminder(UserModel student, String courseTitle, String taskTitle, LocalDate deadline) {
        try {
            String studentEmail = student.getEmail();
            String studentName = buildStudentName(student);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(studentEmail);
            helper.setSubject("Напоминание: завтра дедлайн задания - " + taskTitle);

            String emailBody = buildDeadlineReminderBody(studentName, courseTitle, taskTitle, deadline);

            helper.setText(emailBody, true);

            mailSender.send(message);

            logger.info("Deadline reminder sent successfully to: {} for task: {}", 
                    studentEmail, taskTitle);

        } catch (MessagingException e) {
            logger.error("Error sending deadline reminder: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error sending deadline reminder: {}", e.getMessage(), e);
        }
    }

    private String buildStudentName(UserModel user) {
        StringBuilder name = new StringBuilder();
        if (user.getLastName() != null) {
            name.append(user.getLastName());
        }
        if (user.getFirstName() != null) {
            if (name.length() > 0) name.append(" ");
            name.append(user.getFirstName());
        }
        if (user.getMiddleName() != null && !user.getMiddleName().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(user.getMiddleName());
        }
        return name.length() > 0 ? name.toString() : user.getEmail();
    }

    private String buildGradeNotificationBody(String studentName, String courseTitle, 
                                             String taskTitle, Integer gradeValue, String feedback) {
        String feedbackHtml = feedback != null && !feedback.isEmpty() 
            ? "<p><strong>Комментарий преподавателя:</strong> " + feedback + "</p>" 
            : "";

        return "<html><body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color: #2c3e50;'>Оценка выставлена</h2>" +
                "<p>Уважаемый(ая) <strong>" + studentName + "</strong>,</p>" +
                "<p>Преподаватель выставил оценку за ваше решение задания <strong>\"" + taskTitle + "\"</strong> по курсу <strong>\"" + courseTitle + "\"</strong>.</p>" +
                "<p><strong>Ваша оценка: " + gradeValue + "</strong></p>" +
                feedbackHtml +
                "<p>Вы можете просмотреть детали в вашем личном кабинете.</p>" +
                "<p>С уважением,<br>Команда SupSchool</p>" +
                "</body></html>";
    }

    private String buildDeadlineReminderBody(String studentName, String courseTitle, 
                                            String taskTitle, LocalDate deadline) {
        String deadlineStr = deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        return "<html><body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color: #e74c3c;'>Напоминание о дедлайне</h2>" +
                "<p>Уважаемый(ая) <strong>" + studentName + "</strong>,</p>" +
                "<p>Напоминаем, что <strong>завтра (" + deadlineStr + ")</strong> истекает срок сдачи задания <strong>\"" + taskTitle + "\"</strong> по курсу <strong>\"" + courseTitle + "\"</strong>.</p>" +
                "<p>Пожалуйста, не забудьте отправить решение до указанной даты.</p>" +
                "<p>Вы можете просмотреть задание и отправить решение в вашем личном кабинете.</p>" +
                "<p>С уважением,<br>Команда SupSchool</p>" +
                "</body></html>";
    }
}

