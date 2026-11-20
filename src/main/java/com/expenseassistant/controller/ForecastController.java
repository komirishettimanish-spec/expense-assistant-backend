package com.expenseassistant.controller;

import com.expenseassistant.dto.ForecastInsightResponse;
import com.expenseassistant.dto.SpendingForecastResponse;
import com.expenseassistant.service.CurrentUserService;
import com.expenseassistant.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public SpendingForecastResponse forecast() {
        return forecastService.forecast(currentUserService.getCurrentUser());
    }

    @GetMapping("/insight")
    public ForecastInsightResponse insight() {
        return forecastService.insight(currentUserService.getCurrentUser());
    }
}
