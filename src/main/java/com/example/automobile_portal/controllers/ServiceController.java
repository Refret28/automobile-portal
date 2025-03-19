package com.example.automobile_portal.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import com.example.automobile_portal.models.User;
import com.example.automobile_portal.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@Validated
public class ServiceController {

    @Autowired
    private UserService userService;

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

        model.addAttribute("username", dbUser.getUsername());
        model.addAttribute("user_id", userId);
        return "mainPage";
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
        model.addAttribute("user", new User());
        return "moderatorPage";
    }

    @GetMapping("/user_acc/{userId}")
    public String userAccount(@PathVariable("userId") Integer userId, Model model, HttpSession session) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }
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
