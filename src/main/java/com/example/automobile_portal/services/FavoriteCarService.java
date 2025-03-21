package com.example.automobile_portal.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.automobile_portal.models.FavoriteCar;
import com.example.automobile_portal.repositories.FavoriteCarRepository; 

@Service
public class FavoriteCarService {
    @Autowired
    private FavoriteCarRepository favoriteCarRepository;

    public List<FavoriteCar> getFavoriteCars(int userId) {
        List<FavoriteCar> cars = favoriteCarRepository.findByUserId(userId);
        System.out.println("Полученные избранные автомобили: " + cars);
        return cars;
    }

    public FavoriteCar addFavoriteCar(FavoriteCar favoriteCar) {
        return favoriteCarRepository.save(favoriteCar);
    }

    public void delete(Long carId) {
        favoriteCarRepository.deleteById(carId);
    }   

    public boolean isExistCar(Integer userId, String details){
        FavoriteCar existingFavoriteCar = favoriteCarRepository.findByUserIdAndCarModel(userId, details);

        return existingFavoriteCar != null;
    }
}