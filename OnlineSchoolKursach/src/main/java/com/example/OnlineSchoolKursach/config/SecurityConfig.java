package com.example.OnlineSchoolKursach.config;

import com.example.OnlineSchoolKursach.security.JwtAuthenticationFilter;
import com.example.OnlineSchoolKursach.security.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/reset-password").permitAll()
                        .requestMatchers("/forgot-password").permitAll()
                        .requestMatchers("/forgot-password/me").authenticated()
                        .requestMatchers("/course/**").permitAll()
                        .requestMatchers("/admin").hasAuthority("ROLE_Администратор")
                        .requestMatchers("/teacher/**").hasAnyAuthority("ROLE_Преподаватель", "ROLE_Администратор")
                        .requestMatchers("/teacher").hasAuthority("ROLE_Преподаватель")
                        .requestMatchers("/student").hasAuthority("ROLE_Студент")
                        .requestMatchers("/dashboard").authenticated()

                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        
                        .requestMatchers("/v1/api/auth/**").permitAll()
                        .requestMatchers("/v1/api/test").permitAll()
                        .requestMatchers("/v1/api/files/image").permitAll()
                        .requestMatchers("/v1/api/files/download").authenticated()
                        .requestMatchers("/v1/api/files/upload").hasAnyAuthority("ROLE_Студент", "ROLE_Преподаватель", "ROLE_Администратор")
                        .requestMatchers("/v1/api/files/upload-solution").hasAnyAuthority("ROLE_Студент", "ROLE_Преподаватель", "ROLE_Администратор")
                        .requestMatchers("/v1/api/files/**").authenticated()
                        .requestMatchers("/v1/api/admin/**").hasAuthority("ROLE_Администратор")
                        .requestMatchers("/v1/api/teacher/**").hasAnyAuthority("ROLE_Преподаватель", "ROLE_Администратор")
                        .requestMatchers("/v1/api/student/**").hasAnyAuthority("ROLE_Студент", "ROLE_Преподаватель", "ROLE_Администратор")
                        .requestMatchers("/v1/api/**").authenticated()
                        
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }
}