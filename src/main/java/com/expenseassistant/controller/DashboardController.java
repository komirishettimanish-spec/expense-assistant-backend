package com.expenseassistant.controller;

import com.expenseassistant.dto.DashboardResponse;
import com.expenseassistant.service.CurrentUserService;
import com.expenseassistant.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public DashboardResponse dashboard() {
        return dashboardService.build(currentUserService.getCurrentUser());
    }
}
