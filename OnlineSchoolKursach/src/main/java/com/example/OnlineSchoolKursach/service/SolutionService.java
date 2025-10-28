package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.SolutionModel;
import com.example.OnlineSchoolKursach.model.SolutionStatusModel;
import com.example.OnlineSchoolKursach.model.TaskModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.repository.SolutionRepository;
import com.example.OnlineSchoolKursach.repository.SolutionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SolutionService {

    @Autowired
    private SolutionRepository solutionRepository;
    
    @Autowired
    private SolutionStatusRepository solutionStatusRepository;

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
        // Set default status if not provided
        if (solution.getSolutionStatus() == null) {
            List<SolutionStatusModel> statuses = solutionStatusRepository.findAll();
            if (!statuses.isEmpty()) {
                solution.setSolutionStatus(statuses.get(0)); // Default to first status
            }
        }
        
        // Set submit date if not provided
        if (solution.getSubmitDate() == null) {
            solution.setSubmitDate(LocalDate.now());
        }
        
        return solutionRepository.save(solution);
    }

    public SolutionModel updateSolution(Long solutionId, SolutionModel updatedSolution) {
        Optional<SolutionModel> existingSolutionOpt = solutionRepository.findById(solutionId);
        if (existingSolutionOpt.isPresent()) {
            SolutionModel existingSolution = existingSolutionOpt.get();
            existingSolution.setAnswerText(updatedSolution.getAnswerText());
            existingSolution.setAnswerFile(updatedSolution.getAnswerFile());
            existingSolution.setSolutionStatus(updatedSolution.getSolutionStatus());
            existingSolution.setGrade(updatedSolution.getGrade());
            return solutionRepository.save(existingSolution);
        }
        return null;
    }

    public boolean deleteSolution(Long solutionId) {
        if (solutionRepository.existsById(solutionId)) {
            solutionRepository.deleteById(solutionId);
            return true;
        }
        return false;
    }
    
    public SolutionModel getSolutionByTaskAndUser(Long taskId, Long userId) {
        List<SolutionModel> solutions = solutionRepository.findByTaskTaskId(taskId);
        return solutions.stream()
                .filter(solution -> solution.getUser().getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}