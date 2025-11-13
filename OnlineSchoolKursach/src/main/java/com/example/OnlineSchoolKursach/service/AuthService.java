package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.dto.LoginRequest;
import com.example.OnlineSchoolKursach.dto.LoginResponse;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.UserRepository;
import com.example.OnlineSchoolKursach.repository.RoleRepository;
import com.example.OnlineSchoolKursach.model.RoleModel;
import com.example.OnlineSchoolKursach.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
 private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FileService fileService;

    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            logger.info("Authentication successful for user: {}", loginRequest.getUsername());

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            UserModel user = userRepository.findByEmail(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            user = convertUserImageUrl(user);

            logger.info("JWT token generated for user: {}", user.getEmail());
            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "STUDENT";
            return new LoginResponse(token, user.getEmail(), roleName);
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}, error: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    public UserModel register(UserModel user) {
        logger.info("Attempting registration for user: {}", user.getEmail());
        
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Email already exists: {}", user.getEmail());
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        String encodedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(encodedPassword);
        RoleModel defaultRole = roleRepository.findByRoleName("Студент")
                .orElseThrow(() -> new RuntimeException("Роль Студент не найдена. Проверьте инициализацию ролей."));
        user.setRole(defaultRole);
        logger.info("Password encoded for user: {}", user.getEmail());
        
        UserModel savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getEmail());
        return savedUser;
    }

    public UserModel getUserByEmail(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return convertUserImageUrl(user);
    }

    public UserModel updateUserProfile(String email, UserModel updatedUser) {
        UserModel existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setMiddleName(updatedUser.getMiddleName());
        existingUser.setEmail(updatedUser.getEmail());
        
        if (updatedUser.getImageUrl() != null && !updatedUser.getImageUrl().isEmpty()) {
            existingUser.setImageUrl(updatedUser.getImageUrl());
        }
        
        UserModel savedUser = userRepository.save(existingUser);
        return convertUserImageUrl(savedUser);
    }

    private UserModel convertUserImageUrl(UserModel user) {
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty() 
                && !user.getImageUrl().startsWith("http") 
                && !user.getImageUrl().startsWith("/v1/api/files/image")) {
            try {
                String fullUrl = fileService.getFileUrl(user.getImageUrl());
                if (fullUrl != null) {
                    user.setImageUrl(fullUrl);
                }
            } catch (Exception e) {
                // Log error but don't fail
            }
        }
        return user;
    }
}