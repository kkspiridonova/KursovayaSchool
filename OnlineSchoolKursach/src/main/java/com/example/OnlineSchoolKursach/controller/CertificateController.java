package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.CertificateModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.CertificateRepository;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.MinioFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.InputStream;
import java.util.List;

@Controller
public class CertificateController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private MinioFileService minioFileService;

    /**
     * Страница со списком сертификатов студента
     */
    @GetMapping("/student/certificates")
    public String studentCertificatesPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UserModel user = authService.getUserByEmail(authentication.getName());
        List<CertificateModel> certificates = certificateRepository.findByUserUserId(user.getUserId());

        model.addAttribute("certificates", certificates);
        return "student-certificates";
    }

    /**
     * Скачивание сертификата
     */
    @GetMapping("/student/certificate/{certificateId}/download")
    public ResponseEntity<InputStreamResource> downloadCertificate(
            @PathVariable Long certificateId,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        CertificateModel certificate = certificateRepository.findById(certificateId)
                .orElse(null);

        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем, что сертификат принадлежит текущему пользователю
        UserModel user = authService.getUserByEmail(authentication.getName());
        if (!certificate.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        try {
            InputStream pdfStream = minioFileService.downloadFile(certificate.getFilePath());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=Сертификат_" + certificate.getCertificateNumber() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

