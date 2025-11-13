package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.PasswordResetToken;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.PasswordResetTokenRepository;
import com.example.OnlineSchoolKursach.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);
        
        Optional<UserModel> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            throw new RuntimeException("Пользователь с таким email не найден в системе");
        }

        UserModel user = userOptional.get();
        
        // Delete any existing tokens for this user
        tokenRepository.deleteByUserUserId(user.getUserId());
        
        // Create new token
        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);
        
        // Send email (don't throw exception if email fails - log it instead)
        boolean emailSent = sendPasswordResetEmail(user.getEmail(), token.getToken());
        
        if (emailSent) {
            logger.info("Password reset token created and email sent for user: {}", email);
        } else {
            logger.warn("Password reset token created but email failed to send for user: {}", email);
        }
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        logger.info("Password reset attempt with token");
        
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            logger.warn("Invalid password reset token");
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();
        
        // Check if token is expired
        if (resetToken.isExpired()) {
            logger.warn("Password reset token expired for user: {}", resetToken.getUser().getEmail());
            tokenRepository.delete(resetToken);
            return false;
        }

        // Check if token is already used
        if (resetToken.getUsed()) {
            logger.warn("Password reset token already used for user: {}", resetToken.getUser().getEmail());
            return false;
        }

        // Update password
        UserModel user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        logger.info("Password reset successful for user: {}", user.getEmail());
        return true;
    }

    private boolean sendPasswordResetEmail(String email, String token) {
        try {
            logger.debug("Attempting to send password reset email to: {}", email);
            logger.debug("Using SMTP host: smtp.gmail.com, port: 587");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Восстановление пароля - Онлайн Школа");
            
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            String emailBody = "Здравствуйте!\n\n" +
                    "Вы запросили восстановление пароля для вашего аккаунта в Онлайн Школе.\n\n" +
                    "Для восстановления пароля перейдите по следующей ссылке:\n" +
                    resetUrl + "\n\n" +
                    "Ссылка действительна в течение 24 часов.\n\n" +
                    "Если вы не запрашивали восстановление пароля, проигнорируйте это письмо.\n\n" +
                    "С уважением,\n" +
                    "Команда Онлайн Школы";
            
            message.setText(emailBody);
            logger.debug("Sending email message...");
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", email);
            return true;
        } catch (org.springframework.mail.MailAuthenticationException e) {
            logger.error("=== ОШИБКА АУТЕНТИФИКАЦИИ GMAIL ===");
            logger.error("Не удалось аутентифицироваться в Gmail SMTP");
            logger.error("Возможные причины:");
            logger.error("1. Пароль приложения неправильный или устарел");
            logger.error("2. Двухфакторная аутентификация не включена");
            logger.error("3. Пароль приложения не создан или был удален");
            logger.error("4. Неправильный email адрес");
            logger.error("Решение: https://myaccount.google.com/apppasswords");
            logger.error("Ошибка: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Причина: {}", e.getCause().getMessage());
            }
            
            // Для разработки: выводим ссылку в консоль
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            logger.warn("=== РЕЖИМ РАЗРАБОТКИ: Email не отправлен, но токен создан ===");
            logger.warn("Ссылка для восстановления пароля: {}", resetUrl);
            logger.warn("Токен: {}", token);
            logger.warn("================================================================");
            
            return false;
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}. Error: {}", email, e.getMessage());
            logger.debug("Full email error stack trace:", e);
            
            // Для разработки: выводим ссылку в консоль, если email не отправился
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            logger.warn("=== РЕЖИМ РАЗРАБОТКИ: Email не отправлен, но токен создан ===");
            logger.warn("Ссылка для восстановления пароля: {}", resetUrl);
            logger.warn("Токен: {}", token);
            logger.warn("================================================================");
            
            return false;
        }
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();
        return !resetToken.isExpired() && !resetToken.getUsed();
    }
}

