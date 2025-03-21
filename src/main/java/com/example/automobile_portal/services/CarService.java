package com.example.automobile_portal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import com.example.automobile_portal.models.Car;
import com.example.automobile_portal.repositories.CarRepository;

import java.util.List;

@Service
public class CarService {
    @Autowired
    private CarRepository carRepository;

    public List<Car> getAllCars() {
        return carRepository.findAll(); 
    }

    public Car getCarById(Long carId) {
        Optional<Car> carOptional = carRepository.findById(carId);
        return carOptional.orElse(null);
    }
}
