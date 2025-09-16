package org.example.sansam.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.search.service.SearchSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search-suggestion")
@Slf4j
public class SearchSuggestionController {
    private final SearchSuggestionService searchSuggestionService;

    @GetMapping("/auto")
    public ResponseEntity<?> searchAutoComplete(String keyword) throws IOException {
        List<String> keywordSuggestions = searchSuggestionService.getSuggestions(keyword);
        return ResponseEntity.ok(keywordSuggestions);
    }
}
