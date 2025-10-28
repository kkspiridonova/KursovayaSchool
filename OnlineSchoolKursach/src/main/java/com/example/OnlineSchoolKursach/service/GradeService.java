package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.GradeModel;
import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.model.SolutionStatusModel;
import com.example.OnlineSchoolKursach.repository.GradeRepository;
import com.example.OnlineSchoolKursach.repository.SolutionStatusRepository;
import com.example.OnlineSchoolKursach.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private SolutionRepository solutionRepository;
    
    @Autowired
    private SolutionStatusRepository solutionStatusRepository;

    public GradeModel getGradeById(Long gradeId) {
        Optional<GradeModel> grade = gradeRepository.findById(gradeId);
        return grade.orElse(null);
    }

    public GradeModel createGrade(GradeModel grade) {
        SolutionModel solution = grade.getSolution();
        if (solution == null) {
            throw new RuntimeException("Решение не может быть null при создании оценки");
        }
        
        // Save the grade first
        GradeModel savedGrade = gradeRepository.save(grade);
        
        // Link the grade back to the solution and update solution status to "Проверено"
        solution.setGrade(savedGrade);
        List<SolutionStatusModel> statuses = solutionStatusRepository.findAll();
        for (SolutionStatusModel status : statuses) {
            if ("Проверено".equals(status.getStatusName()) || 
                "Checked".equals(status.getStatusName()) ||
                "Graded".equals(status.getStatusName())) {
                solution.setSolutionStatus(status);
                break;
            }
        }
        solutionRepository.save(solution);
        
        return savedGrade;
    }

    public GradeModel updateGrade(Long gradeId, GradeModel updatedGrade) {
        Optional<GradeModel> existingGradeOpt = gradeRepository.findById(gradeId);
        if (existingGradeOpt.isPresent()) {
            GradeModel existingGrade = existingGradeOpt.get();
            existingGrade.setGradeValue(updatedGrade.getGradeValue());
            existingGrade.setFeedback(updatedGrade.getFeedback());
            return gradeRepository.save(existingGrade);
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