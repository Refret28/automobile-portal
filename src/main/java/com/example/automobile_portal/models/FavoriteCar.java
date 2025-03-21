package com.example.automobile_portal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "favorite_cars", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "car_details"}) 
})
public class FavoriteCar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "car_model")
    private String carModel;
    
    @Column(name = "car_details")
    private String carDetails;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
}
