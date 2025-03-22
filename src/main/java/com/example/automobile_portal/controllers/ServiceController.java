package com.example.automobile_portal.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import com.example.automobile_portal.models.Car;
import com.example.automobile_portal.models.FavoriteCar;
import com.example.automobile_portal.models.News;
import com.example.automobile_portal.models.User;
import com.example.automobile_portal.services.CarService;
import com.example.automobile_portal.services.FavoriteCarService;
import com.example.automobile_portal.services.NewsService;
import com.example.automobile_portal.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@Validated
public class ServiceController {

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private CarService carService;

    @Autowired
    private FavoriteCarService favoriteCarService;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/avatars/";

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "loginPage";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                request.getSession().invalidate();
            }
            return "redirect:/login";
    }

    @GetMapping("/main")
    public String getMainPage(@RequestParam("user_id") int userId, Model model) {
        User dbUser = userService.getUserById(userId);
        if (dbUser == null) {
            return "redirect:/login?error=user_not_found";
        }

        List<News> newsList = newsService.getAllNews();
        List<Car> carList = carService.getAllCars();

        model.addAttribute("username", dbUser.getUsername());
        model.addAttribute("user_id", userId);
        model.addAttribute("newsList", newsList);
        model.addAttribute("carList", carList);
        
        return "mainPage";
    }    

    @PostMapping("/add-to-favorites")
    @ResponseBody
    public ResponseEntity<String> addToFavorites(@RequestParam Integer userId, @RequestParam Long carId) {
        User user = userService.getUserById(userId);
        Car car = carService.getCarById(carId); 
    
        if (user != null && car != null) {
            if (!favoriteCarService.isExistCar(userId, car.getModel())) {
                FavoriteCar favoriteCar = new FavoriteCar();
                favoriteCar.setUser(user);
                favoriteCar.setCarModel(car.getModel());
                favoriteCar.setCarDetails(car.getDetails());
                favoriteCar.setImageUrl(car.getImageUrl());
                favoriteCarService.addFavoriteCar(favoriteCar);
                return ResponseEntity.ok("Автомобиль добавлен в избранное!");
            } else {
                return ResponseEntity.badRequest().body("Автомобиль уже добавлен в избранное!");
            }
        }
        return ResponseEntity.badRequest().body("Пользователь или автомобиль не найден.");
    }    

    @PostMapping("/remove-favorite")
    @ResponseBody
    public ResponseEntity<String> removeFavorite(@RequestParam(required = true) Long userId, 
                                                 @RequestParam(required = true) Long carId) {
        if (userId == null || carId == null) {
            return ResponseEntity.badRequest().body("Ошибка: userId или carId не переданы.");
        }
    
        favoriteCarService.delete(carId);
        return ResponseEntity.ok("Автомобиль удален из избранного!");
    }    

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "registerPage";
    }

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        model.addAttribute("user", new User());
        return "adminPage";
    }

    @GetMapping("/moderator")
    public String getModeratorPage(Model model) {
        List<News> newsList = newsService.getAllNews();
        model.addAttribute("newsList", newsList);
        return "moderatorPage";
    }

    @GetMapping("/user_acc/{userId}")
    public String userAccount(@PathVariable("userId") Integer userId, Model model, HttpSession session) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        List<FavoriteCar> favoriteCars = favoriteCarService.getFavoriteCars(userId);

        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;
        }
        session.setAttribute("visitCount", ++visitCount);

        model.addAttribute("visitCount", visitCount);
        model.addAttribute("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        model.addAttribute("username", user.getUsername());
        model.addAttribute("created_at", user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        model.addAttribute("email", user.getEmail());
        model.addAttribute("phone", user.getPhone());
        model.addAttribute("user_id", user.getId());
        model.addAttribute("avatar", user.getAvatar() != null ? user.getAvatar() : "default.jpg");
        model.addAttribute("avatarFilename", user.getAvatar());
        model.addAttribute("favoriteCars", favoriteCars); 
        
        return "user_accPage";
    }

    @PostMapping("user_acc/{userId}/upload")
    public String uploadAvatar(@PathVariable int userId,
                               @RequestParam("avatar") MultipartFile file, 
                               Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Файл не выбран!");
            return "redirect:/user_acc/" + userId;
        }
    
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
    
            String filename = "avatar_" + userId + ".png";
            Path filePath = uploadPath.resolve(filename);
    
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    
            userService.updateUserAvatar(userId, "/uploads/avatars/" + filename);
    
        } catch (IOException e) {
            model.addAttribute("error", "Ошибка загрузки файла!");
            return "redirect:/user_acc/" + userId;
        }
    
        return "redirect:/user_acc/" + userId;
    }
   
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "registerPage";
        }
        userService.saveUser(user);
        return "redirect:/login";
    }
}
