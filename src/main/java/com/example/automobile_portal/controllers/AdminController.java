package com.example.automobile_portal.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.automobile_portal.models.RoleTranslator;
import com.example.automobile_portal.models.User;
import com.example.automobile_portal.models.UserFile;
import com.example.automobile_portal.services.FileService;
import com.example.automobile_portal.services.UserService;

@Controller
@Validated
public class AdminController {
    @Autowired
    private UserService userService;

     @Autowired
    private FileService fileService;

    @Value("${file.avatar-dir}")
    private String UPLOAD_DIR;

    @Value("${file.files-dir}")
    private String FILES_DIR;

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        List<User> users = userService.getAllUsers();
    
        for (User user : users) {
            String translatedRole = RoleTranslator.translate(user.getRole().getName());
            user.getRole().setName(translatedRole); 
        }

        model.addAttribute("users", users);
        return "adminPage";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getUsers() {
        List<User> users = userService.getAllUsers();
        
        for (User user : users) {
            String translatedRole = RoleTranslator.translate(user.getRole().getName());
            user.getRole().setName(translatedRole);
        }

        return users;
    }

        @PostMapping("/update-role")
    public String updateUserRole(@RequestParam int userId, @RequestParam String role) {
        userService.updateUserRole(userId, role); 
        return "redirect:/admin";
    }


    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Integer userId) {
        userService.deleteUserById(userId); 
        return "redirect:/admin";
    }

    @GetMapping("/edit-user/{id}")
    public String getEditUser(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        List<UserFile> userFiles = fileService.getFilesByUserId(id);

        model.addAttribute("user", user);
        model.addAttribute("files", userFiles); 
        return "editUser";
    }

    @PostMapping("/edit-user")
    public String editUser(@RequestParam("id") Integer userId,
                        @RequestParam("username") String username,
                        @RequestParam("email") String email,
                        @RequestParam("phone") String phone,
                        @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                        @RequestParam(value = "deleteFiles", required = false) List<Integer> deleteFiles) {
        
        User user = userService.getUserById(userId);
        
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);

        if (deleteFiles != null) {
            for (Integer fileId : deleteFiles) {
                fileService.deleteFile(fileId);
            }
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarPath = saveAvatar(userId, avatarFile);
            user.setAvatar(avatarPath);
        }

        userService.saveUser(user);
        return "redirect:/admin";
    }

    private String saveAvatar(int userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Файл не выбран!");
        }
    
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
    
            String filename = "avatar_" + userId + ".png";
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    
            String avatarPath = "/uploads/avatars/" + filename;
            return avatarPath;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла!");
        }
    }    

    @PostMapping("/user_acc/delete-files")
    public String deleteFiles(@RequestParam(value = "deleteFiles", required = false) List<Integer> deleteFiles) {
        System.out.println("Удаление файлов, получено " + (deleteFiles != null ? deleteFiles.size() : 0) + " файлов для удаления");
    
        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            for (Integer fileId : deleteFiles) {
                System.out.println("Удаляем файл с ID: " + fileId);
                UserFile file = fileService.getFileById(fileId);
                if (file != null) {
                    Path filePath = Paths.get(FILES_DIR + "files_" + file.getUser().getUsername() + "/" + file.getFilename());
                    try {
                        Files.deleteIfExists(filePath);
                        System.out.println("Файл удалён: " + filePath);
                    } catch (IOException e) {
                        System.err.println("Ошибка при удалении файла: " + filePath);
                    }
    
                    fileService.deleteFile(fileId);
                }
            }
        }
        return "redirect:/admin";
    }    
    
}
