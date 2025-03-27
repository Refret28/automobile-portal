package com.example.automobile_portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.automobile_portal.models.UserFile;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {
    List<UserFile> findByUserId(Integer userId);
}