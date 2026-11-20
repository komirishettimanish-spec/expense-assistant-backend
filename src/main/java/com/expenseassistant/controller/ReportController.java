package com.expenseassistant.controller;

import com.expenseassistant.dto.MonthlySummaryResponse;
import com.expenseassistant.service.CurrentUserService;
import com.expenseassistant.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserService currentUserService;

    @GetMapping("/monthly")
    public MonthlySummaryResponse monthly(@RequestParam(required = false) Integer month,
                                          @RequestParam(required = false) Integer year) {
        return reportService.monthly(currentUserService.getCurrentUser(), month, year);
    }
}
