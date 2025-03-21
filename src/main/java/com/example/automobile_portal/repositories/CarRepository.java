package com.example.automobile_portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.automobile_portal.models.Car;

public interface CarRepository extends JpaRepository<Car, Long> {
    
}
