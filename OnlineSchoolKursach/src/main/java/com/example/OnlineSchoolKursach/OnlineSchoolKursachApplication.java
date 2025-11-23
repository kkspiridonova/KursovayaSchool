package com.example.OnlineSchoolKursach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.example.OnlineSchoolKursach.repository.RoleRepository;
import com.example.OnlineSchoolKursach.model.RoleModel;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@EnableScheduling
public class OnlineSchoolKursachApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineSchoolKursachApplication.class, args);
	}

    @Bean
    public CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            String[] roles = {"Администратор", "Преподаватель", "Студент"};
            for (String r : roles) {
                if (!roleRepository.existsByRoleName(r)) {
                    roleRepository.save(new RoleModel(r));
                }
            }
        };
    }
}
