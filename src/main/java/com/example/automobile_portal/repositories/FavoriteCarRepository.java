package com.example.automobile_portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.automobile_portal.models.FavoriteCar;

@Repository
public interface FavoriteCarRepository extends JpaRepository<FavoriteCar, Long> {
    List<FavoriteCar> findByUserId(int userId); 
    void deleteById(Long id);
    FavoriteCar findByUserIdAndCarModel(Integer userId, String carModel); 
}