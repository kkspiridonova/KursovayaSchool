package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.model.SolutionStatusModel;
import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.SolutionRepository;
import com.example.OnlineSchoolKursach.repository.SolutionStatusRepository;
import com.example.OnlineSchoolKursach.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SolutionService {

    private static final Logger logger = LoggerFactory.getLogger(SolutionService.class);

    @Autowired
    private SolutionRepository solutionRepository;
    
    @Autowired
    private SolutionStatusRepository solutionStatusRepository;
    
    @Autowired
    private TaskRepository taskRepository;

    public List<SolutionModel> getSolutionsByTaskId(Long taskId) {
        return solutionRepository.findByTaskTaskIdWithGrade(taskId);
    }

    public List<SolutionModel> getSolutionsByUserId(Long userId) {
        return solutionRepository.findByUserUserId(userId);
    }

    public SolutionModel getSolutionById(Long solutionId) {
        Optional<SolutionModel> solution = solutionRepository.findById(solutionId);
        return solution.orElse(null);
    }

    public SolutionModel createSolution(SolutionModel solution) {
        logger.info("Creating solution for task: {}, user: {}", 
            solution.getTask() != null ? solution.getTask().getTaskId() : "null",
            solution.getUser() != null ? solution.getUser().getUserId() : "null");
        
        // Load task from database to get full information including deadline
        TaskModel task = null;
        if (solution.getTask() != null && solution.getTask().getTaskId() != null) {
            Optional<TaskModel> taskOpt = taskRepository.findById(solution.getTask().getTaskId());
            if (taskOpt.isPresent()) {
                task = taskOpt.get();
                solution.setTask(task);
                logger.info("Task loaded: {}, deadline: {}", task.getTaskId(), task.getDeadline());
            } else {
                logger.error("Task not found: {}", solution.getTask().getTaskId());
                throw new RuntimeException("Задание не найдено");
            }
        } else {
            logger.error("Task is null or taskId is null");
            throw new RuntimeException("Задание не указано");
        }
        
        // Disallow submission if task is closed (Прошел)
        if (task.getTaskStatus() != null && "Прошел".equals(task.getTaskStatus().getStatusName())) {
            logger.warn("Task is closed, cannot submit solution");
            throw new RuntimeException("Задание закрыто для сдачи");
        }

        // Set submit date to current date when solution is created
        LocalDate submitDate = LocalDate.now();
        if (solution.getSubmitDate() == null) {
            solution.setSubmitDate(submitDate);
        } else {
            submitDate = solution.getSubmitDate();
        }
        logger.info("Submit date: {}", submitDate);
        
        // Determine status based on deadline and submission time
        // If solution has answer (text or file), set status based on deadline
        boolean hasAnswer = (solution.getAnswerText() != null && !solution.getAnswerText().isEmpty()) ||
                            (solution.getAnswerFile() != null && !solution.getAnswerFile().isEmpty());
        logger.info("Has answer: {}, answerText: {}, answerFile: {}", 
            hasAnswer, 
            solution.getAnswerText() != null ? "present" : "null",
            solution.getAnswerFile() != null ? "present" : "null");
        
        // Always set status - never leave it null
        String statusName;
        if (hasAnswer && task.getDeadline() != null) {
            // Check if submitted after deadline
            if (submitDate.isAfter(task.getDeadline()) || submitDate.isEqual(task.getDeadline())) {
                statusName = "Сдано с опозданием";
            } else {
                statusName = "Сдано";
            }
        } else if (hasAnswer && task.getDeadline() == null) {
            // If has answer but no deadline, set to "Сдано"
            statusName = "Сдано";
        } else {
            // If no answer yet, set status to "Назначено"
            statusName = "Назначено";
        }
        
        logger.info("Setting solution status to: {}", statusName);
        setSolutionStatusByName(solution, statusName);
        logger.info("Solution status set: {}", solution.getSolutionStatus() != null ? solution.getSolutionStatus().getStatusName() : "null");
        
        logger.info("Saving solution...");
        SolutionModel saved = solutionRepository.save(solution);
        logger.info("Solution saved successfully with ID: {}", saved.getSolutionId());
        return saved;
    }

    public SolutionModel updateSolution(Long solutionId, SolutionModel updatedSolution) {
        Optional<SolutionModel> existingSolutionOpt = solutionRepository.findById(solutionId);
        if (existingSolutionOpt.isPresent()) {
            SolutionModel existingSolution = existingSolutionOpt.get();
            existingSolution.setAnswerText(updatedSolution.getAnswerText());
            existingSolution.setAnswerFile(updatedSolution.getAnswerFile());
            // При апдейте ответа определяем статус:
            // если установлен файл/текст и есть дедлайн у задания — вычисляем вовремя/с опозданием
            TaskModel task = existingSolution.getTask();
            if ((updatedSolution.getAnswerText() != null && !updatedSolution.getAnswerText().isEmpty()) ||
                (updatedSolution.getAnswerFile() != null && !updatedSolution.getAnswerFile().isEmpty())) {
                existingSolution.setSubmitDate(LocalDate.now());
                if (task != null && task.getDeadline() != null && (LocalDate.now().isAfter(task.getDeadline()) || LocalDate.now().isEqual(task.getDeadline()))) {
                    setSolutionStatusByName(existingSolution, "Сдано с опозданием");
                } else {
                    setSolutionStatusByName(existingSolution, "Сдано");
                }
            }
            // Оценка может устанавливаться отдельно, статус при этом не меняем
            existingSolution.setGrade(updatedSolution.getGrade());
            return solutionRepository.save(existingSolution);
        }
        return null;
    }

    private void setSolutionStatusByName(SolutionModel solution, String statusName) {
        logger.info("Looking for solution status: {}", statusName);
        List<SolutionStatusModel> statuses = solutionStatusRepository.findAll();
        logger.info("Found {} solution statuses in database", statuses.size());
        for (SolutionStatusModel s : statuses) {
            logger.info("  - Status ID: {}, Name: '{}'", s.getSolutionStatusId(), s.getStatusName());
        }
        
        SolutionStatusModel status = statuses.stream()
                .filter(s -> statusName.equals(s.getStatusName()))
                .findFirst()
                .orElseThrow(() -> {
                    // Log available statuses for debugging
                    String availableStatuses = statuses.stream()
                            .map(SolutionStatusModel::getStatusName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("нет статусов");
                    logger.error("Status '{}' not found. Available statuses: {}", statusName, availableStatuses);
                    return new RuntimeException("Статус решения '" + statusName + "' не найден в базе данных. Доступные статусы: " + availableStatuses);
                });
        solution.setSolutionStatus(status);
        logger.info("Solution status set to: {} (ID: {})", status.getStatusName(), status.getSolutionStatusId());
    }

    public boolean deleteSolution(Long solutionId) {
        if (solutionRepository.existsById(solutionId)) {
            solutionRepository.deleteById(solutionId);
            return true;
        }
        return false;
    }
    
    public SolutionModel getSolutionByTaskAndUser(Long taskId, Long userId) {
        return solutionRepository.findFirstByTaskTaskIdAndUserUserId(taskId, userId);
    }

    // Обновление просроченных решений на "Просрочено"
    // Помечает как "Просрочено" только решения со статусом "Назначено", у которых дедлайн прошел
    public int markOverdueSolutions(List<SolutionModel> solutions) {
        int updated = 0;
        LocalDate today = LocalDate.now();
        for (SolutionModel solution : solutions) {
            TaskModel task = solution.getTask();
            if (task != null && task.getDeadline() != null) {
                // Check if solution has status "Назначено" and deadline has passed
                String currentStatus = solution.getSolutionStatus() != null ? solution.getSolutionStatus().getStatusName() : null;
                boolean isAssigned = "Назначено".equals(currentStatus);
                boolean deadlinePassed = today.isAfter(task.getDeadline()) || today.isEqual(task.getDeadline());
                
                // Also check if solution has no answer (text or file)
                boolean hasNoAnswer = (solution.getAnswerText() == null || solution.getAnswerText().isEmpty())
                        && (solution.getAnswerFile() == null || solution.getAnswerFile().isEmpty());
                
                if (isAssigned && deadlinePassed && hasNoAnswer) {
                    setSolutionStatusByName(solution, "Просрочено");
                    solutionRepository.save(solution);
                    updated++;
                }
            }
        }
        return updated;
    }
}