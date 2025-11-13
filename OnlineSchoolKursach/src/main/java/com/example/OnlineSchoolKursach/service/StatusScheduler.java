package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.repository.CourseRepository;
import com.example.OnlineSchoolKursach.repository.TaskRepository;
import com.example.OnlineSchoolKursach.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatusScheduler {

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SolutionService solutionService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CourseService courseService;

    // Каждый час: обновить просроченные решения и закрыть задачи с прошедшим дедлайном
    @Scheduled(cron = "0 0 * * * *")
    public void updateStatusesHourly() {
        try {
            // Получаем только ID решений, чтобы избежать дубликатов от JOIN'ов
            List<Long> solutionIds = solutionRepository.findAllDistinctIds();
            List<SolutionModel> solutions = new java.util.ArrayList<>();
            
            // Загружаем каждое решение отдельно, чтобы избежать проблем с дубликатами
            for (Long solutionId : solutionIds) {
                try {
                    solutionRepository.findById(solutionId).ifPresent(solutions::add);
                } catch (Exception e) {
                    // Пропускаем проблемные записи
                    System.err.println("Error loading solution " + solutionId + ": " + e.getMessage());
                }
            }
            
            solutionService.markOverdueSolutions(solutions);

            List<TaskModel> tasks = taskRepository.findAll();
            taskService.closePassedTasks(tasks);
        } catch (Exception e) {
            // Log error but don't crash the scheduler
            System.err.println("Error in updateStatusesHourly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Каждые 6 часов: проверить статусы курсов по датам/вместимости
    @Scheduled(cron = "0 0 */6 * * *")
    public void updateCourseStatuses() {
        try {
            List<CourseModel> courses = courseRepository.findAll();
            for (CourseModel c : courses) {
                courseService.updateCourseStatusByDatesAndCapacity(c);
            }
        } catch (Exception e) {
            // Log error but don't crash the scheduler
            System.err.println("Error in updateCourseStatuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
}





