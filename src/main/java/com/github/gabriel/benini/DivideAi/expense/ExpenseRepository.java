package com.github.gabriel.benini.DivideAi.expense;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    boolean existsByGroupId(Long groupId);

    boolean existsByGroupIdAndPaidById(
            Long groupId,
            Long paidByUserId
    );
}