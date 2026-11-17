package com.github.gabriel.benini.DivideAi.balance;

import com.github.gabriel.benini.DivideAi.balance.dto.GroupBalanceResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.GroupSettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    private final TransferService transferService;

    @GetMapping("/{groupId}/balance")
    public ResponseEntity<GroupBalanceResponse> calculateGroupBalance(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                balanceService.calculateGroupBalance(groupId)
        );
    }

    @GetMapping("/{groupId}/settlement")
    public ResponseEntity<GroupSettlementResponse> calculateSettlement(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                transferService.calculateSettlement(groupId)
        );
    }
}