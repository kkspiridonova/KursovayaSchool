package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.model.TaskStatusModel;
import com.example.OnlineSchoolKursach.model.LessonModel;
import com.example.OnlineSchoolKursach.model.EnrollmentModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.TaskRepository;
import com.example.OnlineSchoolKursach.repository.TaskStatusRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private MinioFileService minioFileService;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public List<TaskModel> getTasksByLessonId(Long lessonId) {
        return taskRepository.findByLessonLessonId(lessonId);
    }

    public TaskModel getTaskById(Long taskId) {
        Optional<TaskModel> task = taskRepository.findById(taskId);
        return task.orElse(null);
    }

    public TaskModel createTask(TaskModel task) {
        if (task.getTaskStatus() == null) {
            List<TaskStatusModel> statuses = taskStatusRepository.findAll();
            if (!statuses.isEmpty()) {
                task.setTaskStatus(statuses.get(0));
            }
        }

        if (task.getDeadline() == null) {
            task.setDeadline(LocalDate.now().plusDays(7));
        }
        
        return taskRepository.save(task);
    }

    public void closeTaskIfDeadlinePassed(TaskModel task) {
        if (task.getDeadline() != null && LocalDate.now().isAfter(task.getDeadline())) {
            List<TaskStatusModel> statuses = taskStatusRepository.findAll();
            statuses.stream()
                    .filter(s -> "Прошел".equals(s.getStatusName()))
                    .findFirst()
                    .ifPresent(task::setTaskStatus);
            taskRepository.save(task);
        }
    }

    public List<TaskStatusModel> getAllTaskStatuses() {
        return taskStatusRepository.findAll();
    }

    public TaskModel updateTask(Long taskId, TaskModel updatedTask, MultipartFile newFile) {
        Optional<TaskModel> existingTaskOpt = taskRepository.findById(taskId);
        if (existingTaskOpt.isPresent()) {
            TaskModel existingTask = existingTaskOpt.get();
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setDeadline(updatedTask.getDeadline());
            existingTask.setTaskStatus(updatedTask.getTaskStatus());

            if (newFile != null && !newFile.isEmpty()) {
                if (existingTask.getAttachedFile() != null && !existingTask.getAttachedFile().isEmpty()) {
                    try {
                        minioFileService.deleteFile(existingTask.getAttachedFile());
                    } catch (Exception e) {
                        System.err.println("Error deleting old task file: " + e.getMessage());
                    }
                }

                try {
                    String filePath = minioFileService.uploadFile(newFile, "task");
                    existingTask.setAttachedFile(filePath);
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при загрузке файла: " + e.getMessage());
                }
            } else if (updatedTask.getAttachedFile() != null && !updatedTask.getAttachedFile().isEmpty()) {
                existingTask.setAttachedFile(updatedTask.getAttachedFile());
            }
            
            return taskRepository.save(existingTask);
        }
        return null;
    }

    public boolean deleteTask(Long taskId) {
        Optional<TaskModel> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            TaskModel task = taskOpt.get();
            if (task.getAttachedFile() != null && !task.getAttachedFile().isEmpty()) {
                try {
                    minioFileService.deleteFile(task.getAttachedFile());
                } catch (Exception e) {
                    System.err.println("Error deleting task file: " + e.getMessage());
                }
            }
            
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public int closePassedTasks(List<TaskModel> tasks) {
        int count = 0;
        for (TaskModel task : tasks) {
            if (task.getDeadline() != null && LocalDate.now().isAfter(task.getDeadline())) {
                List<TaskStatusModel> statuses = taskStatusRepository.findAll();
                Optional<TaskStatusModel> passed = statuses.stream().filter(s -> "Прошел".equals(s.getStatusName())).findFirst();
                if (passed.isPresent() && (task.getTaskStatus() == null || !"Прошел".equals(task.getTaskStatus().getStatusName()))) {
                    task.setTaskStatus(passed.get());
                    taskRepository.save(task);
                    count++;
                }
            }
        }
        return count;
    }

    public List<TaskModel> getStudentTasks(UserModel user) {
        List<EnrollmentModel> enrollments = enrollmentRepository.findByUserUserId(user.getUserId());
        
        List<Long> courseIds = enrollments.stream()
                .filter(e -> e.getEnrollmentStatus() != null && 
                           ("Активен".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активный".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Активна".equals(e.getEnrollmentStatus().getStatusName()) || 
                            "Active".equals(e.getEnrollmentStatus().getStatusName())))
                .map(e -> e.getCourse().getCourseId())
                .collect(Collectors.toList());
        
        List<TaskModel> allTasks = taskRepository.findAll();
        
        return allTasks.stream()
                .filter(task -> {
                    LessonModel lesson = task.getLesson();
                    if (lesson == null || lesson.getCourse() == null) {
                        return false;
                    }
                    return courseIds.contains(lesson.getCourse().getCourseId());
                })
                .collect(Collectors.toList());
    }

    public List<TaskModel> getStudentTasksForWeek(UserModel user, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<TaskModel> studentTasks = getStudentTasks(user);
        
        return studentTasks.stream()
                .filter(task -> task.getDeadline() != null && 
                               !task.getDeadline().isBefore(weekStart) && 
                               !task.getDeadline().isAfter(weekEnd))
                .collect(Collectors.toList());
    }

    public List<TaskModel> getTeacherTasks(UserModel teacher) {
        List<TaskModel> allTasks = taskRepository.findAll();
        
        return allTasks.stream()
                .filter(task -> {
                    LessonModel lesson = task.getLesson();
                    if (lesson == null || lesson.getCourse() == null) {
                        return false;
                    }
                    return lesson.getCourse().getTeacher() != null && 
                           lesson.getCourse().getTeacher().getUserId().equals(teacher.getUserId());
                })
                .collect(Collectors.toList());
    }

    public List<TaskModel> getTeacherTasksForWeek(UserModel teacher, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<TaskModel> teacherTasks = getTeacherTasks(teacher);
        
        return teacherTasks.stream()
                .filter(task -> task.getDeadline() != null && 
                               !task.getDeadline().isBefore(weekStart) && 
                               !task.getDeadline().isAfter(weekEnd))
                .collect(Collectors.toList());
    }
}