package com.github.gabriel.benini.DivideAi.balance;

import com.github.gabriel.benini.DivideAi.balance.dto.GroupBalanceResponse;
import com.github.gabriel.benini.DivideAi.common.exception.BusinessException;
import com.github.gabriel.benini.DivideAi.expense.Expense;
import com.github.gabriel.benini.DivideAi.expense.ExpenseRepository;
import com.github.gabriel.benini.DivideAi.group.Group;
import com.github.gabriel.benini.DivideAi.group.GroupMember;
import com.github.gabriel.benini.DivideAi.group.GroupMemberRepository;
import com.github.gabriel.benini.DivideAi.group.GroupRepository;
import com.github.gabriel.benini.DivideAi.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BalanceService balanceService;

    private Group group;
    private List<GroupMember> members;

    @BeforeEach
    void setUp() {
        group = new Group();
        group.setId(1L);
        group.setName("Beach Trip");

        members = List.of(
                createMember(1L, "Gabriel"),
                createMember(2L, "Lucas"),
                createMember(3L, "Ana")
        );
    }

    @Test
    void shouldCalculateEqualSharesAndBalances() {
        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository.findByGroupIdOrderByUserIdAsc(1L))
                .thenReturn(members);

        when(expenseRepository.findByGroupIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        createExpense(30000L, members.get(0).getUser()),
                        createExpense(10000L, members.get(1).getUser()),
                        createExpense(5000L, members.get(2).getUser())
                ));

        GroupBalanceResponse result =
                balanceService.calculateGroupBalance(1L);

        assertEquals(45000L, result.totalAmountInCents());
        assertEquals(3, result.participantCount());

        assertEquals(15000L,
                result.participants().get(0).balanceInCents());

        assertEquals(-5000L,
                result.participants().get(1).balanceInCents());

        assertEquals(-10000L,
                result.participants().get(2).balanceInCents());

        assertEquals(0L, result.participants().stream()
                .mapToLong(p -> p.balanceInCents())
                .sum());
    }

    @Test
    void shouldDistributeRemainingCentsCorrectly() {
        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository.findByGroupIdOrderByUserIdAsc(1L))
                .thenReturn(members);

        when(expenseRepository.findByGroupIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        createExpense(10000L, members.get(0).getUser())
                ));

        GroupBalanceResponse result =
                balanceService.calculateGroupBalance(1L);

        assertEquals(3334L,
                result.participants().get(0).shareAmountInCents());

        assertEquals(3333L,
                result.participants().get(1).shareAmountInCents());

        assertEquals(3333L,
                result.participants().get(2).shareAmountInCents());

        long totalShares = result.participants().stream()
                .mapToLong(p -> p.shareAmountInCents())
                .sum();

        assertEquals(10000L, totalShares);
    }

    @Test
    void shouldRejectGroupWithoutMembers() {
        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository.findByGroupIdOrderByUserIdAsc(1L))
                .thenReturn(List.of());

        assertThrows(
                BusinessException.class,
                () -> balanceService.calculateGroupBalance(1L)
        );

        verifyNoInteractions(expenseRepository);
    }

    private GroupMember createMember(Long userId, String name) {
        User user = new User();
        user.setId(userId);
        user.setName(name);

        GroupMember member = new GroupMember();
        member.setUser(user);
        member.setGroup(group);

        return member;
    }

    private Expense createExpense(Long amount, User payer) {
        Expense expense = new Expense();
        expense.setAmountInCents(amount);
        expense.setPaidBy(payer);
        expense.setGroup(group);

        return expense;
    }
}