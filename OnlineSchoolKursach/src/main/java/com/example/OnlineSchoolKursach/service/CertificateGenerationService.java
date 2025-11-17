package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.CertificateModel;
import com.example.OnlineSchoolKursach.model.CertificateStatusModel;
import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.CertificateRepository;
import com.example.OnlineSchoolKursach.repository.CertificateStatusRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class CertificateGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateGenerationService.class);

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MinioFileService minioFileService;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateStatusRepository certificateStatusRepository;

    @Transactional
    public CertificateModel generateCertificate(UserModel user, CourseModel course) {
        try {
            String certificateNumber = generateCertificateNumber(course.getCourseId(), user.getUserId());

            String studentName = buildStudentName(user);

            String htmlContent = generateHtmlTemplate(studentName, course.getTitle(), 
                    LocalDate.now(), certificateNumber);

            byte[] pdfBytes = convertHtmlToPdf(htmlContent);

            String filePath = savePdfToMinio(pdfBytes, certificateNumber);

            CertificateModel certificate = new CertificateModel(user, course, certificateNumber);
            certificate.setFilePath(filePath);
            certificate.setIssueDate(LocalDate.now());

            CertificateStatusModel status = certificateStatusRepository.findAll().stream()
                    .filter(s -> "Выдан".equals(s.getStatusName()))
                    .findFirst()
                    .orElseGet(() -> {
                        CertificateStatusModel newStatus = new CertificateStatusModel("Выдан");
                        return certificateStatusRepository.save(newStatus);
                    });
            certificate.setCertificateStatus(status);

            CertificateModel savedCertificate = certificateRepository.save(certificate);
            logger.info("Certificate generated successfully: {} for user: {} course: {}", 
                    certificateNumber, user.getEmail(), course.getTitle());

            return savedCertificate;
        } catch (Exception e) {
            logger.error("Error generating certificate for user {} course {}: {}", 
                    user.getEmail(), course.getTitle(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при генерации сертификата: " + e.getMessage(), e);
        }
    }

    private String generateCertificateNumber(Long courseId, Long userId) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("CERT-%d-%d-%s", courseId, userId, timestamp);
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

    private String generateHtmlTemplate(String studentName, String courseTitle, 
                                       LocalDate issueDate, String certificateNumber) {
        Context context = new Context(Locale.forLanguageTag("ru"));
        context.setVariable("studentName", studentName);
        context.setVariable("courseTitle", courseTitle);
        context.setVariable("issueDate", issueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        context.setVariable("certificateNumber", certificateNumber);

        return templateEngine.process("certificate-template", context);
    }

    private byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error converting HTML to PDF: {}", e.getMessage(), e);
            throw new IOException("Ошибка при конвертации HTML в PDF", e);
        }
    }

    private String savePdfToMinio(byte[] pdfBytes, String certificateNumber) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);

            String objectName = "certificates/" + certificateNumber + ".pdf";

            minioFileService.uploadBytes(pdfBytes, objectName, "application/pdf");
            
            return objectName;
        } catch (Exception e) {
            logger.error("Error saving PDF to MinIO: {}", e.getMessage(), e);
            throw new IOException("Ошибка при сохранении PDF в MinIO", e);
        }
    }
}

