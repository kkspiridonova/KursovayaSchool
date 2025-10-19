package com.example.OnlineSchoolKursach.config;

import com.example.OnlineSchoolKursach.model.CategoryModel;
import com.example.OnlineSchoolKursach.model.EnrollmentStatusModel;
import com.example.OnlineSchoolKursach.repository.CategoryRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EnrollmentStatusRepository enrollmentStatusRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            CategoryModel math = new CategoryModel("Математика", "Математические дисциплины");
            categoryRepository.save(math);
            
            CategoryModel physics = new CategoryModel("Физика", "Физические дисциплины");
            categoryRepository.save(physics);
            
            CategoryModel chemistry = new CategoryModel("Химия", "Химические дисциплины");
            categoryRepository.save(chemistry);
            
            CategoryModel biology = new CategoryModel("Биология", "Биологические дисциплины");
            categoryRepository.save(biology);
            
            CategoryModel programming = new CategoryModel("Программирование", "Курсы по программированию");
            categoryRepository.save(programming);
            
            CategoryModel languages = new CategoryModel("Языки", "Изучение иностранных языков");
            categoryRepository.save(languages);
        }

        if (enrollmentStatusRepository.count() == 0) {
            EnrollmentStatusModel pending = new EnrollmentStatusModel();
            pending.setStatusName("Ожидает подтверждения");
            enrollmentStatusRepository.save(pending);
            
            EnrollmentStatusModel active = new EnrollmentStatusModel();
            active.setStatusName("Активный");
            enrollmentStatusRepository.save(active);
            
            EnrollmentStatusModel cancelled = new EnrollmentStatusModel();
            cancelled.setStatusName("Отменена");
            enrollmentStatusRepository.save(cancelled);
        }
    }
}