package com.example.OnlineSchoolKursach.repository;

import com.example.OnlineSchoolKursach.model.FileModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileModel, Long> {
    List<FileModel> findAllByOrderByUploadDateDesc();
    List<FileModel> findByUploadedByOrderByUploadDateDesc(UserModel uploadedBy);
}


