package com.example.automobile_portal.services;

import com.example.automobile_portal.models.Role;
import com.example.automobile_portal.models.User;
import com.example.automobile_portal.repositories.RoleRepository;
import com.example.automobile_portal.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service 
@Slf4j 
public class UserService{
    private final UserRepository userRepository; 
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() { 
        return userRepository.findAll(); 
    } 

    public User getUserById(Integer userId) { 
        Optional<User> OptionalUser = userRepository.findById(userId); 
        if (OptionalUser.isPresent()) { 
            return OptionalUser.get(); 
        } 
        log.info( "Пользователь с идентификатором: {} не существует" , userId); 
        return  null ; 
    } 

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }
    
    @Transactional
    public void saveUser(User user) { 
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (user.getRole() == null) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Роль 'USER' не найдена в базе данных"));
            user.setRole(defaultRole);
        }

        userRepository.save(user);
    }  

    public User updateUser(User user) { 
        Optional<User> existingUser = userRepository.findById(user.getId());
        user.setCreatedAt(existingUser.get().getCreatedAt());
        user.setUpdatedAt(LocalDateTime.now());

        User  updatedUser  = userRepository.save(user); 

        log.info( "Сотрудник с идентификатором: {} успешно обновлен" , user.getId()); 
        return updatedUser; 
    } 

    public  void  deleteUserById(Integer id) { 
        userRepository.deleteById(id); 
    } 

    public boolean userExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent(); 
    }

    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null); 
    }

    public void updateUserAvatar(int userId, String avatarPath) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setAvatar(avatarPath);
        userRepository.save(user);
    }

    public User updateUserRole(int userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    
        Role setRole = roleRepository.findByName(role)
            .orElseThrow(() -> new RuntimeException("Роль " + role + " не найдена в базе данных"));
    
        user.setRole(setRole);
        
        return userRepository.save(user);
    }
}
