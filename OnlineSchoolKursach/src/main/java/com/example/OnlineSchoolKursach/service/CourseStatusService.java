package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.CourseStatusModel;
import com.example.OnlineSchoolKursach.repository.CourseStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseStatusService {

    @Autowired
    private CourseStatusRepository courseStatusRepository;

    public List<CourseStatusModel> getAllCourseStatuses() {
        return courseStatusRepository.findAll();
    }
}