package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.CertificateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.time.LocalDate;

@Service
public class CertificateEmailService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateEmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MinioFileService minioFileService;

    /**
     * Отправляет сертификат на email студента
     */
    @Transactional
    public boolean sendCertificateByEmail(CertificateModel certificate) {
        try {
            if (certificate.getFilePath() == null || certificate.getFilePath().isEmpty()) {
                logger.error("Certificate file path is empty for certificate: {}", certificate.getCertificateId());
                return false;
            }

            // Получаем PDF из MinIO
            InputStream pdfStream = minioFileService.downloadFile(certificate.getFilePath());

            // Создаем email сообщение
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String studentEmail = certificate.getUser().getEmail();
            String studentName = buildStudentName(certificate.getUser());
            String courseTitle = certificate.getCourse().getTitle();

            helper.setTo(studentEmail);
            helper.setSubject("Сертификат о прохождении курса: " + courseTitle);

            String emailBody = buildEmailBody(studentName, courseTitle, certificate.getIssueDate(), 
                    certificate.getCertificateNumber());

            helper.setText(emailBody, true); // true = HTML

            // Прикрепляем PDF сертификата
            helper.addAttachment("Сертификат_" + certificate.getCertificateNumber() + ".pdf", 
                    new InputStreamResource(pdfStream), "application/pdf");

            // Отправляем email
            mailSender.send(message);

            // Обновляем статус отправки
            certificate.setEmailSent(true);
            certificate.setEmailSentDate(LocalDate.now());

            logger.info("Certificate email sent successfully to: {} for certificate: {}", 
                    studentEmail, certificate.getCertificateNumber());
            return true;

        } catch (MessagingException e) {
            logger.error("Error sending certificate email: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Error sending certificate email: {}", e.getMessage(), e);
            return false;
        }
    }

    private String buildStudentName(com.example.OnlineSchoolKursach.model.UserModel user) {
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

    private String buildEmailBody(String studentName, String courseTitle, 
                                  LocalDate issueDate, String certificateNumber) {
        return "<html><body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color: #2c3e50;'>Поздравляем!</h2>" +
                "<p>Уважаемый(ая) <strong>" + studentName + "</strong>,</p>" +
                "<p>Вы успешно завершили курс <strong>\"" + courseTitle + "\"</strong>!</p>" +
                "<p>В приложении вы найдете ваш сертификат о прохождении курса.</p>" +
                "<p><strong>Номер сертификата:</strong> " + certificateNumber + "</p>" +
                "<p><strong>Дата выдачи:</strong> " + issueDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "</p>" +
                "<p>Сертификат также доступен в вашем личном кабинете на сайте.</p>" +
                "<p>Желаем дальнейших успехов в обучении!</p>" +
                "<p>С уважением,<br>Команда Онлайн Школы</p>" +
                "</body></html>";
    }
}

