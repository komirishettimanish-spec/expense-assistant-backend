package com.expenseassistant.repository;

import com.expenseassistant.entity.Budget;
import com.expenseassistant.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdOrderByYearDescMonthDescCategoryAsc(Long userId);

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(Long userId, Category category, Integer month, Integer year);
}
