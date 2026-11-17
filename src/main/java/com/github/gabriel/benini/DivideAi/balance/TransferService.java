package com.github.gabriel.benini.DivideAi.balance;

import com.github.gabriel.benini.DivideAi.balance.dto.GroupBalanceResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.GroupSettlementResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.ParticipantBalanceResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final BalanceService balanceService;

    @Transactional(readOnly = true)
    public GroupSettlementResponse calculateSettlement(Long groupId) {

        GroupBalanceResponse balance =
                balanceService.calculateGroupBalance(groupId);

        List<ParticipantBalanceResponse> debtors =
                balance.participants()
                        .stream()
                        .filter(participant ->
                                participant.balanceInCents() < 0
                        )
                        .toList();

        List<ParticipantBalanceResponse> creditors =
                balance.participants()
                        .stream()
                        .filter(participant ->
                                participant.balanceInCents() > 0
                        )
                        .toList();

        List<TransferResponse> transfers = new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        long remainingDebt = 0;
        long remainingCredit = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            ParticipantBalanceResponse debtor =
                    debtors.get(debtorIndex);

            ParticipantBalanceResponse creditor =
                    creditors.get(creditorIndex);

            if (remainingDebt == 0) {
                remainingDebt = Math.negateExact(
                        debtor.balanceInCents()
                );
            }

            if (remainingCredit == 0) {
                remainingCredit = creditor.balanceInCents();
            }

            long transferAmount = Math.min(
                    remainingDebt,
                    remainingCredit
            );

            transfers.add(
                    new TransferResponse(
                            debtor.userId(),
                            debtor.userName(),
                            creditor.userId(),
                            creditor.userName(),
                            transferAmount
                    )
            );

            remainingDebt -= transferAmount;
            remainingCredit -= transferAmount;

            if (remainingDebt == 0) {
                debtorIndex++;
            }

            if (remainingCredit == 0) {
                creditorIndex++;
            }
        }

        return new GroupSettlementResponse(
                balance.groupId(),
                balance.groupName(),
                balance.totalAmountInCents(),
                balance.participantCount(),
                balance.participants(),
                transfers
        );
    }
}