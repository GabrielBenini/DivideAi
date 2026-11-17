package com.github.gabriel.benini.DivideAi.balance;

import com.github.gabriel.benini.DivideAi.balance.dto.GroupBalanceResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.ParticipantBalanceResponse;
import com.github.gabriel.benini.DivideAi.common.exception.BusinessException;
import com.github.gabriel.benini.DivideAi.common.exception.ResourceNotFoundException;
import com.github.gabriel.benini.DivideAi.expense.Expense;
import com.github.gabriel.benini.DivideAi.expense.ExpenseRepository;
import com.github.gabriel.benini.DivideAi.group.Group;
import com.github.gabriel.benini.DivideAi.group.GroupMember;
import com.github.gabriel.benini.DivideAi.group.GroupMemberRepository;
import com.github.gabriel.benini.DivideAi.group.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public GroupBalanceResponse calculateGroupBalance(Long groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with ID: " + groupId
                        )
                );

        List<GroupMember> members =
                groupMemberRepository.findByGroupIdOrderByUserIdAsc(groupId);

        if (members.isEmpty()) {
            throw new BusinessException(
                    "Cannot calculate balance for a group without members"
            );
        }

        List<Expense> expenses =
                expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        long totalAmountInCents = expenses.stream()
                .mapToLong(Expense::getAmountInCents)
                .reduce(0L, Math::addExact);

        int participantCount = members.size();

        long baseShare = totalAmountInCents / participantCount;
        long remainder = totalAmountInCents % participantCount;

        Map<Long, Long> totalPaidByUser = new HashMap<>();

        for (Expense expense : expenses) {
            Long userId = expense.getPaidBy().getId();

            totalPaidByUser.merge(
                    userId,
                    expense.getAmountInCents(),
                    Math::addExact
            );
        }

        List<ParticipantBalanceResponse> participants =
                new java.util.ArrayList<>();

        for (int i = 0; i < members.size(); i++) {

            GroupMember member = members.get(i);

            Long userId = member.getUser().getId();
            String userName = member.getUser().getName();

            long totalPaid = totalPaidByUser.getOrDefault(userId, 0L);

            long shareAmount = baseShare;

            if (i < remainder) {
                shareAmount++;
            }

            long balance = Math.subtractExact(totalPaid, shareAmount);

            participants.add(
                    new ParticipantBalanceResponse(
                            userId,
                            userName,
                            totalPaid,
                            shareAmount,
                            balance
                    )
            );
        }

        return new GroupBalanceResponse(
                group.getId(),
                group.getName(),
                totalAmountInCents,
                participantCount,
                participants
        );
    }
}