package com.github.gabriel.benini.DivideAi.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    boolean existsByGroupId(Long groupId);

    boolean existsByGroupIdAndPaidById(
            Long groupId,
            Long paidByUserId
    );
}