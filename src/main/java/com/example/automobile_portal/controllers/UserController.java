package com.example.automobile_portal.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.nio.file.Path;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.example.automobile_portal.models.Car;
import com.example.automobile_portal.models.FavoriteCar;
import com.example.automobile_portal.models.News;
import com.example.automobile_portal.models.User;
import com.example.automobile_portal.models.UserFile;
import com.example.automobile_portal.services.CarService;
import com.example.automobile_portal.services.FavoriteCarService;
import com.example.automobile_portal.services.FileService;
import com.example.automobile_portal.services.NewsService;
import com.example.automobile_portal.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@Validated
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private CarService carService;

    @Autowired
    private FavoriteCarService favoriteCarService;

    @Value("${file.avatar-dir}")
    private String UPLOAD_DIR;

    @Value("${file.files-dir}")
    private String FILES_DIR;

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
    
    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "registerPage";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "registerPage";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
        return "redirect:/login";
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

    @GetMapping("/user_acc/{userId}")
    public String getUserAccount(@PathVariable("userId") Integer userId, Model model, HttpSession session) {
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

    private ResponseEntity<Resource> serveFile(String directory, String filename) {
        Path filePath = Paths.get(directory).resolve(filename);
        Resource fileResource = new FileSystemResource(filePath);
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache()) 
            .body(fileResource);
    }    

    @GetMapping("/uploads/avatars/{filename}")
    public ResponseEntity<Resource> serveAvatar(@PathVariable String filename) {
        return serveFile(UPLOAD_DIR, filename);
    }

    @GetMapping("/uploads/files/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        return serveFile(FILES_DIR, filename);
    }

    @PostMapping("user_acc/{userId}/upload")
public String uploadAvatar(@PathVariable int userId,
                           @RequestParam("avatar") MultipartFile file) {      
    if (file.isEmpty()) {
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
        e.printStackTrace();
        return "redirect:/user_acc/" + userId;
    }

    return "redirect:/user_acc/" + userId;
}


    @PostMapping("/user_acc/files/upload-file")
    public String uploadFile(@RequestParam("file") MultipartFile file, Principal principal, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Файл не выбран!");
            return "redirect:/user_acc"; 
        }
    
        User user = userService.getUserByEmail(principal.getName()); 
        String username = user.getUsername(); 
    
        Path userDir = Paths.get(FILES_DIR + "/files_" + username); 
    
        try {
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
            }
    
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            String baseName = originalFilename;
    
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex != -1) {
                fileExtension = originalFilename.substring(dotIndex);
                baseName = originalFilename.substring(0, dotIndex);
            }
    
            String newFilename = originalFilename;
            Path filePath = userDir.resolve(newFilename);
            int copyCount = 1;
    
            while (Files.exists(filePath)) {
                newFilename = baseName + "_копия" + copyCount + fileExtension;
                filePath = userDir.resolve(newFilename);
                copyCount++;
            }
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
    
            String relativePath = "/uploads/files/files_" + username + "/" + newFilename;
            fileService.saveFilePath(user.getId(), newFilename, relativePath);
    
            return "redirect:/user_acc/" + user.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки файла!");
            return "redirect:/user_acc"; 
        }
    }    

    @GetMapping("/files/user-files")
    public ResponseEntity<List<UserFile>> getUserFiles(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<UserFile> files = fileService.getFilesByUserId(user.getId());

        return ResponseEntity.ok(files);
    }
    
    @DeleteMapping("/files/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Integer fileId, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        UserFile file = fileService.getFileById(fileId);
    
        if (file == null || !file.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа!");
        }
    
        Path filePath =  Paths.get(file.getFilePath()).normalize();
    
        System.out.println("Путь до файла: " + filePath.toAbsolutePath());
  
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                System.out.println("Файл успешно удален: " + filePath);
                fileService.deleteFile(fileId);  
                return ResponseEntity.ok("Файл удален");
            } else {
                System.out.println("Файл не найден или уже удален: " + filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Файл не найден");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка удаления файла");
        }
    }

}
