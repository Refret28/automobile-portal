package com.example.automobile_portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class NewsController {
    @Autowired
    private NewsService newsService;

    @PostMapping("/add-news")
    public String addNews(@RequestParam String title, @RequestParam String content) {
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        newsService.createNews(news); 
        return "redirect:/moderator"; 
    }

    @PostMapping("/delete-news")
    public String deleteNews(@RequestParam Long id) {
        newsService.deleteNews(id); 
        return "redirect:/moderator"; 
    }

    @GetMapping("/edit-news/{id}")
    public String editNews(@PathVariable Long id, Model model) {
        News news = newsService.getNewsById(id);
        model.addAttribute("news", news);
        return "edit-news"; 
    }

    @PostMapping("/update-news")
    public String updateNews(@ModelAttribute News news) {
        newsService.updateNews(news);
        return "redirect:/moderator"; 
    }

    @GetMapping("/news-list")
    @ResponseBody 
    public List<News> getNewsList() {
        return newsService.getAllNews(); 
    }
}
