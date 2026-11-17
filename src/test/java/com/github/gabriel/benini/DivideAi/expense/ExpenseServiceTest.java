package com.github.gabriel.benini.DivideAi.expense;

import com.github.gabriel.benini.DivideAi.common.exception.BusinessException;
import com.github.gabriel.benini.DivideAi.expense.dto.CreateExpenseRequest;
import com.github.gabriel.benini.DivideAi.expense.dto.ExpenseResponse;
import com.github.gabriel.benini.DivideAi.group.Group;
import com.github.gabriel.benini.DivideAi.group.GroupMemberRepository;
import com.github.gabriel.benini.DivideAi.group.GroupRepository;
import com.github.gabriel.benini.DivideAi.user.User;
import com.github.gabriel.benini.DivideAi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void shouldCreateExpenseWhenPayerIsGroupMember() {
        Group group = new Group();
        group.setId(1L);
        group.setName("Beach Trip");

        User payer = new User();
        payer.setId(1L);
        payer.setName("Gabriel");

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Dinner",
                        15000L,
                        1L,
                        1L
                );

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(payer));

        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 1L))
                .thenReturn(true);

        when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> {
                    Expense expense = invocation.getArgument(0);
                    expense.setId(10L);
                    return expense;
                });

        ExpenseResponse response = expenseService.create(request);

        assertEquals(10L, response.id());
        assertEquals("Dinner", response.description());
        assertEquals(15000L, response.amountInCents());
        assertEquals(1L, response.paidByUserId());
        assertEquals(1L, response.groupId());

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void shouldRejectPayerWhoIsNotGroupMember() {
        Group group = new Group();
        group.setId(1L);

        User payer = new User();
        payer.setId(2L);

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        "Lunch",
                        5000L,
                        2L,
                        1L
                );

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(payer));

        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 2L))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> expenseService.create(request)
        );

        assertEquals(
                "Payer must be a member of the group",
                exception.getMessage()
        );

        verify(expenseRepository, never())
                .save(any(Expense.class));
    }
}