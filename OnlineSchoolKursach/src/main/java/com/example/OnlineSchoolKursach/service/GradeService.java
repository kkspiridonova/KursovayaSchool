package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.GradeModel;
import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.model.SolutionStatusModel;
import com.example.OnlineSchoolKursach.repository.GradeRepository;
import com.example.OnlineSchoolKursach.repository.SolutionStatusRepository;
import com.example.OnlineSchoolKursach.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    private static final Logger logger = LoggerFactory.getLogger(GradeService.class);

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private SolutionRepository solutionRepository;
    
    @Autowired
    private SolutionStatusRepository solutionStatusRepository;

    @Autowired
    private NotificationService notificationService;

    public GradeModel getGradeById(Long gradeId) {
        Optional<GradeModel> grade = gradeRepository.findById(gradeId);
        return grade.orElse(null);
    }

    @Transactional
    public GradeModel createGrade(GradeModel grade) {
        SolutionModel solution = grade.getSolution();
        if (solution == null) {
            throw new RuntimeException("Решение не может быть null при установке оценки");
        }

        GradeModel targetGrade = null;
        if (grade.getGradeId() != null) {
            targetGrade = gradeRepository.findById(grade.getGradeId())
                    .orElseThrow(() -> new RuntimeException("Оценка с id=" + grade.getGradeId() + " не найдена"));
        } else if (grade.getGradeValue() != null) {
            targetGrade = gradeRepository.findFirstByGradeValue(grade.getGradeValue());
            if (targetGrade == null) {
                Long gradeId = Long.valueOf(grade.getGradeValue());
                targetGrade = gradeRepository.findById(gradeId).orElse(null);
            }
            if (targetGrade == null) {
                throw new RuntimeException("Оценка со значением " + grade.getGradeValue() + " не найдена. Убедитесь, что в БД есть оценки от 0 до 5.");
            }
        } else {
            throw new RuntimeException("Не указаны ни id оценки, ни значение оценки");
        }

        logger.info("Linking solution {} to existing grade id={}, value={}",
                solution.getSolutionId(), targetGrade.getGradeId(), targetGrade.getGradeValue());

        solution.setGrade(targetGrade);
        SolutionModel savedSolution = solutionRepository.save(solution);
        logger.info("Solution {} updated with grade_id={}", solution.getSolutionId(), targetGrade.getGradeId());

        try {
            notificationService.sendGradeNotification(targetGrade, savedSolution);
        } catch (Exception e) {
            logger.error("Error sending grade notification: {}", e.getMessage(), e);
        }

        return targetGrade;
    }

    @Transactional
    public GradeModel updateGrade(Long gradeId, GradeModel updatedGrade) {
        Optional<GradeModel> existingGradeOpt = gradeRepository.findById(gradeId);
        if (existingGradeOpt.isPresent()) {
            GradeModel existingGrade = existingGradeOpt.get();
            existingGrade.setGradeValue(updatedGrade.getGradeValue());
            existingGrade.setFeedback(updatedGrade.getFeedback());
            GradeModel g = gradeRepository.save(existingGrade);
            logger.info("Grade {} updated: value={}, feedback='{}'", g.getGradeId(), g.getGradeValue(), g.getFeedback());

            if (updatedGrade.getSolution() != null && updatedGrade.getSolution().getSolutionId() != null) {
                SolutionModel solution = updatedGrade.getSolution();
                solution.setGrade(g);
                solutionRepository.save(solution);
                logger.info("Solution {} ensured linked to grade {}", solution.getSolutionId(), g.getGradeId());
            }
            return g;
        }
        return null;
    }

    public boolean deleteGrade(Long gradeId) {
        if (gradeRepository.existsById(gradeId)) {
            gradeRepository.deleteById(gradeId);
            return true;
        }
        return false;
    }
}