package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.CheckModel;
import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.model.PaymentStatusModel;
import com.example.OnlineSchoolKursach.repository.CheckRepository;
import com.example.OnlineSchoolKursach.repository.PaymentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CheckService {

    @Autowired
    private CheckRepository checkRepository;
    
    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    public CheckModel createCheck(UserModel user, CourseModel course, BigDecimal amount) {
        // Find or create payment status "Оплачено"
        PaymentStatusModel paidStatus = findOrCreatePaymentStatus("Оплачено");
        
        CheckModel check = new CheckModel();
        check.setUser(user);
        check.setCourse(course);
        check.setAmount(amount);
        check.setPaymentDate(LocalDate.now());
        check.setPaymentStatus(paidStatus);
        
        return checkRepository.save(check);
    }
    
    public List<CheckModel> getChecksByUser(UserModel user) {
        return checkRepository.findByUserUserId(user.getUserId());
    }
    
    private PaymentStatusModel findOrCreatePaymentStatus(String statusName) {
        List<PaymentStatusModel> statuses = paymentStatusRepository.findAll();
        for (PaymentStatusModel status : statuses) {
            if (statusName.equals(status.getStatusName())) {
                return status;
            }
        }
        
        // If not found, create it
        PaymentStatusModel newStatus = new PaymentStatusModel();
        newStatus.setStatusName(statusName);
        return paymentStatusRepository.save(newStatus);
    }
}