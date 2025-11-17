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

    @Scheduled(cron = "0 0 * * * *")
    public void updateStatusesHourly() {
        try {
            List<Long> solutionIds = solutionRepository.findAllDistinctIds();
            List<SolutionModel> solutions = new java.util.ArrayList<>();

            for (Long solutionId : solutionIds) {
                try {
                    solutionRepository.findById(solutionId).ifPresent(solutions::add);
                } catch (Exception e) {
                    System.err.println("Error loading solution " + solutionId + ": " + e.getMessage());
                }
            }
            
            solutionService.markOverdueSolutions(solutions);

            List<TaskModel> tasks = taskRepository.findAll();
            taskService.closePassedTasks(tasks);
        } catch (Exception e) {
            System.err.println("Error in updateStatusesHourly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void updateCourseStatuses() {
        try {
            List<CourseModel> courses = courseRepository.findAll();
            for (CourseModel c : courses) {
                courseService.updateCourseStatusByDatesAndCapacity(c);
            }
        } catch (Exception e) {
            System.err.println("Error in updateCourseStatuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
