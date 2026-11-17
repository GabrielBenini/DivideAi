package com.github.gabriel.benini.DivideAi.expense;

import com.github.gabriel.benini.DivideAi.expense.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        ExpenseResponse response = expenseService.create(request);

        return ResponseEntity
                .created(URI.create("/api/expenses/" + response.id()))
                .body(response);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> findById(
            @PathVariable Long expenseId
    ) {
        return ResponseEntity.ok(
                expenseService.findById(expenseId)
        );
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> findByGroupId(
            @RequestParam Long groupId
    ) {
        return ResponseEntity.ok(
                expenseService.findByGroupId(groupId)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<GroupExpenseSummaryResponse> getGroupSummary(
            @RequestParam Long groupId
    ) {
        return ResponseEntity.ok(
                expenseService.getGroupSummary(groupId)
        );
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {
        return ResponseEntity.ok(
                expenseService.update(expenseId, request)
        );
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long expenseId
    ) {
        expenseService.delete(expenseId);
        return ResponseEntity.noContent().build();
    }
}