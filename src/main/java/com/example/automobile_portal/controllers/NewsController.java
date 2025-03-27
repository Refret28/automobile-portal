package com.example.automobile_portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.automobile_portal.models.News;
import com.example.automobile_portal.services.NewsService;
import org.springframework.security.core.Authentication;

@Controller
public class NewsController {
    @Autowired
    private NewsService newsService;

    public String redirectByRole(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        
        return "redirect:/moderator"; 
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @PostMapping("/add-news")
    public String addNews(@RequestParam String title, @RequestParam String content) {
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        newsService.createNews(news); 
        
        return redirectByRole();
    }

    @PostMapping("/delete-news")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public String deleteNews(@RequestParam Long id) {
        newsService.deleteNews(id); 
        return redirectByRole();
    }


    @GetMapping("/edit-news/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public String editNews(@PathVariable Long id, Model model) {
        News news = newsService.getNewsById(id);
        model.addAttribute("news", news);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ROLE_ADMIN")) {
            return "edit-news-admin";
        }
        
        return "edit-news-moder"; 

    }
    
    @PostMapping("/update-news")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public String updateNews(@ModelAttribute News news) {
        newsService.updateNews(news);
        return "redirect:/moderator"; 
    }

    @GetMapping("/news-list")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @ResponseBody 
    public List<News> getNewsList() {
        return newsService.getAllNews(); 
    }
}
