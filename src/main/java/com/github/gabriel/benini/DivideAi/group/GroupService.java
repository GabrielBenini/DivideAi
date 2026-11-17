package com.github.gabriel.benini.DivideAi.group;

import com.github.gabriel.benini.DivideAi.common.exception.BusinessException;
import com.github.gabriel.benini.DivideAi.common.exception.ResourceNotFoundException;
import com.github.gabriel.benini.DivideAi.expense.ExpenseRepository;
import com.github.gabriel.benini.DivideAi.group.dto.*;
import com.github.gabriel.benini.DivideAi.user.User;
import com.github.gabriel.benini.DivideAi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        User creator = findUserOrThrow(request.createdByUserId());

        Group group = new Group();
        group.setName(request.name());

        Group savedGroup = groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setGroup(savedGroup);
        member.setUser(creator);

        groupMemberRepository.save(member);

        return toGroupResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public GroupResponse findById(Long groupId) {
        return toGroupResponse(findGroupOrThrow(groupId));
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> findAll() {
        return groupRepository.findAll()
                .stream()
                .map(this::toGroupResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> findByUserId(Long userId) {
        findUserOrThrow(userId);

        List<Long> groupIds = groupMemberRepository.findByUserId(userId)
                .stream()
                .map(member -> member.getGroup().getId())
                .toList();

        if (groupIds.isEmpty()) {
            return List.of();
        }

        return groupRepository.findAllById(groupIds)
                .stream()
                .map(this::toGroupResponse)
                .toList();
    }

    @Transactional
    public GroupResponse update(Long groupId, UpdateGroupRequest request) {
        Group group = findGroupOrThrow(groupId);

        group.setName(request.name());

        return toGroupResponse(group);
    }

    @Transactional
    public void delete(Long groupId) {
        Group group = findGroupOrThrow(groupId);

        if (expenseRepository.existsByGroupId(groupId)) {
            throw new BusinessException(
                    "Cannot delete a group that contains expenses"
            );
        }

        groupMemberRepository.deleteByGroupId(groupId);
        groupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> findMembers(Long groupId) {
        findGroupOrThrow(groupId);

        return groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public GroupMemberResponse addMember(
            Long groupId,
            AddGroupMemberRequest request
    ) {
        Group group = findGroupOrThrow(groupId);
        User user = findUserOrThrow(request.userId());

        if (groupMemberRepository.existsByGroupIdAndUserId(
                groupId, user.getId()
        )) {
            throw new BusinessException(
                    "User is already a member of this group"
            );
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);

        GroupMember savedMember = groupMemberRepository.save(member);

        return toMemberResponse(savedMember);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        findGroupOrThrow(groupId);

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User is not a member of this group"
                        )
                );

        if (groupMemberRepository.countByGroupId(groupId) <= 1) {
            throw new BusinessException(
                    "Cannot remove the last member of a group"
            );
        }

        if (expenseRepository.existsByGroupIdAndPaidById(
                groupId, userId
        )) {
            throw new BusinessException(
                    "Cannot remove a member who has registered expenses"
            );
        }

        groupMemberRepository.delete(member);
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

    private GroupResponse toGroupResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCreatedAt()
        );
    }

    private GroupMemberResponse toMemberResponse(GroupMember member) {
        return new GroupMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getName(),
                member.getJoinedAt()
        );
    }
}