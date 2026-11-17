package com.github.gabriel.benini.DivideAi.expense;

import com.github.gabriel.benini.DivideAi.common.exception.BusinessException;
import com.github.gabriel.benini.DivideAi.common.exception.ResourceNotFoundException;
import com.github.gabriel.benini.DivideAi.expense.dto.*;
import com.github.gabriel.benini.DivideAi.group.Group;
import com.github.gabriel.benini.DivideAi.group.GroupMemberRepository;
import com.github.gabriel.benini.DivideAi.group.GroupRepository;
import com.github.gabriel.benini.DivideAi.user.User;
import com.github.gabriel.benini.DivideAi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        Group group = findGroupOrThrow(request.groupId());
        User payer = findUserOrThrow(request.paidByUserId());

        validateGroupMembership(group.getId(), payer.getId());

        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setAmountInCents(request.amountInCents());
        expense.setPaidBy(payer);
        expense.setGroup(group);

        Expense savedExpense = expenseRepository.save(expense);

        return toResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long expenseId) {
        Expense expense = findExpenseOrThrow(expenseId);
        return toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findByGroupId(Long groupId) {
        findGroupOrThrow(groupId);

        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse update(
            Long expenseId,
            UpdateExpenseRequest request
    ) {
        Expense expense = findExpenseOrThrow(expenseId);
        User payer = findUserOrThrow(request.paidByUserId());

        validateGroupMembership(
                expense.getGroup().getId(),
                payer.getId()
        );

        expense.setDescription(request.description());
        expense.setAmountInCents(request.amountInCents());
        expense.setPaidBy(payer);

        return toResponse(expense);
    }

    @Transactional
    public void delete(Long expenseId) {
        Expense expense = findExpenseOrThrow(expenseId);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public GroupExpenseSummaryResponse getGroupSummary(Long groupId) {
        findGroupOrThrow(groupId);

        List<Expense> expenses =
                expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        long totalAmountInCents = expenses.stream()
                .mapToLong(Expense::getAmountInCents)
                .reduce(0L, Math::addExact);

        return new GroupExpenseSummaryResponse(
                groupId,
                totalAmountInCents,
                expenses.size()
        );
    }

    private void validateGroupMembership(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(
                groupId, userId
        )) {
            throw new BusinessException(
                    "Payer must be a member of the group"
            );
        }
    }

    private Expense findExpenseOrThrow(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with ID: " + expenseId
                        )
                );
    }

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with ID: " + groupId
                        )
                );
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId
                        )
                );
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmountInCents(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getGroup().getId(),
                expense.getCreatedAt()
        );
    }
}