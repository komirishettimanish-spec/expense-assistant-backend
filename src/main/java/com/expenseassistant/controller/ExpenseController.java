package com.expenseassistant.controller;

import com.expenseassistant.dto.ExpenseRequest;
import com.expenseassistant.dto.ExpenseResponse;
import com.expenseassistant.service.CurrentUserService;
import com.expenseassistant.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(currentUserService.getCurrentUser(), request);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return expenseService.list(currentUserService.getCurrentUser(), category, fromDate, toDate);
    }

    @GetMapping("/category/{category}")
    public List<ExpenseResponse> byCategory(@PathVariable String category) {
        return expenseService.list(currentUserService.getCurrentUser(), category, null, null);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return expenseService.get(currentUserService.getCurrentUser(), id);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.update(currentUserService.getCurrentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(currentUserService.getCurrentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
