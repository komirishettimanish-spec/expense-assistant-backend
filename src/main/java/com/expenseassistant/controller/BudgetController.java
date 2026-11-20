package com.expenseassistant.controller;

import com.expenseassistant.dto.BudgetRequest;
import com.expenseassistant.dto.BudgetResponse;
import com.expenseassistant.dto.BudgetStatusResponse;
import com.expenseassistant.service.BudgetService;
import com.expenseassistant.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody BudgetRequest request) {
        return budgetService.create(currentUserService.getCurrentUser(), request);
    }

    @GetMapping
    public List<BudgetResponse> list() {
        return budgetService.list(currentUserService.getCurrentUser());
    }

    @GetMapping("/status")
    public List<BudgetStatusResponse> status(@RequestParam(required = false) Integer month,
                                             @RequestParam(required = false) Integer year) {
        return budgetService.status(currentUserService.getCurrentUser(), month, year);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return budgetService.update(currentUserService.getCurrentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.delete(currentUserService.getCurrentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
