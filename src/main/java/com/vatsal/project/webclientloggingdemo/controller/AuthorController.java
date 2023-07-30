package com.vatsal.project.webclientloggingdemo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vatsal.project.webclientloggingdemo.dto.Author;
import com.vatsal.project.webclientloggingdemo.utils.RestUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1/author", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthorController {
    @Autowired
    private RestUtility restUtility;

    @PostMapping(value = "/")
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        ObjectMapper objectMapper = new ObjectMapper();
        String authorString  = null;
        try {
            authorString = objectMapper.writeValueAsString(author);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        author = restUtility.sendRequest("http://127.0.0.1:8080/projects/server/logging/demo", "/v1/author/", HttpMethod.POST, authorString, Author.class);
        return ResponseEntity.ok(author);
    }
}
