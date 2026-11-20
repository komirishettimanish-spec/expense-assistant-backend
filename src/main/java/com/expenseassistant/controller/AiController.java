package com.expenseassistant.controller;

import com.expenseassistant.dto.SavingSuggestionResponse;
import com.expenseassistant.service.CurrentUserService;
import com.expenseassistant.service.SavingSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SavingSuggestionService savingSuggestionService;
    private final CurrentUserService currentUserService;

    @GetMapping("/saving-suggestions")
    public SavingSuggestionResponse savingSuggestions() {
        return savingSuggestionService.suggest(currentUserService.getCurrentUser());
    }
}
