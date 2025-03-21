package com.example.automobile_portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.automobile_portal.models.News;

public interface NewsRepository extends JpaRepository<News, Long> {
    
}