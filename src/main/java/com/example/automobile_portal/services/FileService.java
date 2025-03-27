package com.example.automobile_portal.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.automobile_portal.models.User;
import com.example.automobile_portal.models.UserFile;
import com.example.automobile_portal.repositories.UserFileRepository;

@Service
public class FileService {
    
    @Autowired
    private UserFileRepository fileRepository;

    public List<UserFile> getFilesByUserId(Integer userId) {
        return fileRepository.findByUserId(userId);
    }

    public void saveFilePath(Integer userId, String filename, String relativePath) {
        UserFile userFile = new UserFile();
        userFile.setUser(new User(userId)); 
        userFile.setFilename(filename);
        userFile.setFilePath(relativePath);
        userFile.setUploadedAt(LocalDateTime.now());

        fileRepository.save(userFile);
    }

    public UserFile getFileById(Integer fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }
    
    public void deleteFile(Integer fileId) {
        fileRepository.deleteById(fileId);
    }
}
