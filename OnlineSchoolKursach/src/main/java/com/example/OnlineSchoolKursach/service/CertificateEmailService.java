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

    @Autowired
    private com.example.OnlineSchoolKursach.repository.CertificateRepository certificateRepository;

    @Transactional
    public boolean sendCertificateByEmail(CertificateModel certificate) {
        try {
            if (mailSender == null) {
                logger.error("JavaMailSender is not configured. Check email settings in application.yaml");
                return false;
            }

            String filePath = resolveFilePath(certificate);
            if (filePath == null) {
                logger.error("Certificate file path is empty for certificate: {}", certificate.getCertificateId());
                return false;
            }

            InputStream pdfStream = minioFileService.downloadFileWithFallback(filePath);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String studentEmail = certificate.getUser().getEmail();
            String studentName = buildStudentName(certificate.getUser());
            String courseTitle = certificate.getCourse().getTitle();

            helper.setTo(studentEmail);
            helper.setSubject("Сертификат о прохождении курса: " + courseTitle);

            String emailBody = buildEmailBody(studentName, courseTitle, certificate.getIssueDate(), 
                    certificate.getCertificateNumber());

            helper.setText(emailBody, true);

            helper.addAttachment("Сертификат_" + certificate.getCertificateNumber() + ".pdf", 
                    new InputStreamResource(pdfStream), "application/pdf");

            mailSender.send(message);

            certificate.setEmailSent(true);
            certificate.setEmailSentDate(LocalDate.now());
            certificateRepository.save(certificate);

            logger.info("Certificate email sent successfully to: {} for certificate: {}", 
                    studentEmail, certificate.getCertificateNumber());
            return true;

        } catch (org.springframework.mail.MailAuthenticationException e) {
            logger.error("=== ОШИБКА АУТЕНТИФИКАЦИИ EMAIL ===");
            logger.error("Не удалось аутентифицироваться в SMTP сервере");
            logger.error("Проверьте настройки MAIL_USERNAME и MAIL_PASSWORD в .env файле");
            logger.error("Для Gmail используйте пароль приложения: https://myaccount.google.com/apppasswords");
            logger.error("Ошибка: {}", e.getMessage(), e);
            return false;
        } catch (MessagingException e) {
            logger.error("Ошибка отправки email: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Ошибка отправки email: {}", e.getMessage(), e);
            if (e.getCause() != null) {
                logger.error("Причина: {}", e.getCause().getMessage());
            }
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
                "<p>С уважением,<br>Команда SupSchool</p>" +
                "</body></html>";
    }

    private String resolveFilePath(CertificateModel certificate) {
        if (certificate.getFilePath() != null && !certificate.getFilePath().isBlank()) {
            return certificate.getFilePath();
        }
        if (certificate.getDocumentFile() != null && !certificate.getDocumentFile().isBlank()) {
            return certificate.getDocumentFile();
        }
        return null;
    }
}

