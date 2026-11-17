package com.github.gabriel.benini.DivideAi.group;

import com.github.gabriel.benini.DivideAi.group.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> create(
            @Valid @RequestBody CreateGroupRequest request
    ) {
        GroupResponse response = groupService.create(request);

        return ResponseEntity
                .created(URI.create("/api/groups/" + response.id()))
                .body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> findById(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(groupService.findById(groupId));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> findAll(
            @RequestParam(required = false) Long userId
    ) {
        if (userId != null) {
            return ResponseEntity.ok(
                    groupService.findByUserId(userId)
            );
        }

        return ResponseEntity.ok(groupService.findAll());
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> update(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        return ResponseEntity.ok(
                groupService.update(groupId, request)
        );
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long groupId
    ) {
        groupService.delete(groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> findMembers(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                groupService.findMembers(groupId)
        );
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMemberRequest request
    ) {
        GroupMemberResponse response =
                groupService.addMember(groupId, request);

        return ResponseEntity
                .created(URI.create(
                        "/api/groups/" + groupId
                                + "/members/" + response.userId()
                ))
                .body(response);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}