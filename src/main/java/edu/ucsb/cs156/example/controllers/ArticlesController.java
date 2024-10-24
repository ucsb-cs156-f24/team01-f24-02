package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.*;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
/**
 * This is a REST controller for Articles
 */

@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {
    @Autowired
    ArticlesController articlesController;

    @Operation(summary= "List all Articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Articles> allArticles() {
        Iterable<Articles> articles = ArticlesRepository.findAll();
        return articles;
    }

    /**
     * This method creates a new diningcommons. Accessible only to users with the role "ROLE_ADMIN".
     * @param id long id
     * @param title of the article
     * @param url article url
     * @param explaination an explaination of article
     * @param email email of the sender
     * @param date added date of article
     * @return the save article
     */
    @Operation(summary= "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Articles postArticle(
        @Parameter(name="title") @RequestParam String title,
        @Parameter(name="url") @RequestParam String url,
        @Parameter(name="explaination") @RequestParam String explaination,
        @Parameter(name="email") @RequestParam String email,
        @Parameter(name="localDateTime", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("localDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime)
            throws JsonProcessingException {
        log.info("localDateTime={}", localDateTime);

        Articles articles = Articles.builder()
            .title(title)
            .url(url)
            .explanation(email)
            .dateAdded(localDateTime)
            .build();

        Articles savedArticles = ArticlesRepository.save(articles);

        return savedArticles;
    }
    /**
     * Get a single article by id
     * 
     * @param id the id of the date
     * @return an article
     */
    @Operation(summary= "Get a single date")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Articles getById(
    @Parameter(name="id") @RequestParam Long id) {
            Articles article = ArticlesRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));
       
            return article;
    
    }
    
    @Operation(summary= "Update a single article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Articles updateaArticles(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid Articles incoming) {

        Articles articles = articles.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(articles.class, id));

        articles.setTitle(incoming.getTitle());
        articles.setUrl (incoming.getUrl());
        articles.setExplanation(incoming.getExplanation());
        articles.setEmail(incoming.getEmail());
        articles.setDateAdded(incoming.getDateadded());
        

        ArticlesRepository.save(articles);

        return articles;
    }

    /**
     * Delete an article
     * 
     * @param id the id of thearticle to delete
     * @return a message indicating the article was deleted
     */
    @Operation(summary= "Delete an article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteArticle(
            @Parameter(name="id") @RequestParam Long id) {
        Articles article = ArticlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        ArticlesRepository.delete(article);
        return genericMessage("Article with id %s deleted".formatted(id));
    }

}
