package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.model.TaskStatusModel;
import com.example.OnlineSchoolKursach.model.LessonModel;
import com.example.OnlineSchoolKursach.repository.TaskRepository;
import com.example.OnlineSchoolKursach.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    
    @Autowired
    private MinioFileService minioFileService;

    public List<TaskModel> getTasksByLessonId(Long lessonId) {
        return taskRepository.findByLessonLessonId(lessonId);
    }

    public TaskModel getTaskById(Long taskId) {
        Optional<TaskModel> task = taskRepository.findById(taskId);
        return task.orElse(null);
    }

    public TaskModel createTask(TaskModel task) {
        // Set default status if not provided
        if (task.getTaskStatus() == null) {
            List<TaskStatusModel> statuses = taskStatusRepository.findAll();
            if (!statuses.isEmpty()) {
                task.setTaskStatus(statuses.get(0)); // Default to first status
            }
        }
        
        // Set default deadline if not provided
        if (task.getDeadline() == null) {
            task.setDeadline(LocalDate.now().plusDays(7)); // Default to 7 days from now
        }
        
        return taskRepository.save(task);
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
            
            // Handle file update
            if (newFile != null && !newFile.isEmpty()) {
                // Delete old file if exists
                if (existingTask.getAttachedFile() != null && !existingTask.getAttachedFile().isEmpty()) {
                    try {
                        minioFileService.deleteFile(existingTask.getAttachedFile());
                    } catch (Exception e) {
                        System.err.println("Error deleting old task file: " + e.getMessage());
                    }
                }
                // Upload new file
                try {
                    String filePath = minioFileService.uploadFile(newFile, "task");
                    existingTask.setAttachedFile(filePath);
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при загрузке файла: " + e.getMessage());
                }
            } else if (updatedTask.getAttachedFile() != null && !updatedTask.getAttachedFile().isEmpty()) {
                // Keep existing file path if new file not provided
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
            
            // Delete attached file from MinIO if exists
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
}