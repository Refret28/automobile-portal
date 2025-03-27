package com.example.automobile_portal.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.automobile_portal.models.News;
import com.example.automobile_portal.services.NewsService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/moderator")
@Validated
public class ModeratorController {
    @Autowired
    private NewsService newsService;

    @GetMapping
    public String getModeratorPage(Model model) {
        List<News> newsList = newsService.getAllNews();
        model.addAttribute("newsList", newsList);
        return "moderatorPage";
    }

    @PostMapping("/update-news")
    @ResponseBody
    public ResponseEntity<String> updateNews(@ModelAttribute News news) {
        newsService.updateNews(news);
        return ResponseEntity.ok("Новость обновлена");
    }

}
