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

            UserModel user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            logger.info("JWT token generated for user: {}", user.getUsername());
            String roleName = user.getRole() != null ? user.getRole().getName() : "STUDENT";
            return new LoginResponse(token, user.getUsername(), roleName);
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}, error: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    public UserModel register(UserModel user) {
        logger.info("Attempting registration for user: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Username already exists: {}", user.getUsername());
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Email already exists: {}", user.getEmail());
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        RoleModel defaultRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Роль STUDENT не найдена. Проверьте инициализацию ролей."));
        user.setRole(defaultRole);
        logger.info("Password encoded for user: {}", user.getUsername());
        
        UserModel savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public UserModel getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
