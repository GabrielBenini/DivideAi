package com.github.gabriel.benini.DivideAi.balance;

import com.github.gabriel.benini.DivideAi.balance.dto.GroupBalanceResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.GroupSettlementResponse;
import com.github.gabriel.benini.DivideAi.balance.dto.ParticipantBalanceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private TransferService transferService;

    @Test
    void shouldGenerateCorrectTransfers() {
        GroupBalanceResponse balance = createBalance(
                List.of(
                        participant(1L, "Gabriel", 15000L),
                        participant(2L, "Lucas", -5000L),
                        participant(3L, "Ana", -10000L)
                )
        );

        when(balanceService.calculateGroupBalance(1L))
                .thenReturn(balance);

        GroupSettlementResponse result =
                transferService.calculateSettlement(1L);

        assertEquals(2, result.transfers().size());

        var first = result.transfers().get(0);

        assertEquals(2L, first.fromUserId());
        assertEquals(1L, first.toUserId());
        assertEquals(5000L, first.amountInCents());

        var second = result.transfers().get(1);

        assertEquals(3L, second.fromUserId());
        assertEquals(1L, second.toUserId());
        assertEquals(10000L, second.amountInCents());

        long totalTransferred = result.transfers().stream()
                .mapToLong(t -> t.amountInCents())
                .sum();

        assertEquals(15000L, totalTransferred);
    }

    @Test
    void shouldReturnNoTransfersWhenBalancesAreZero() {
        GroupBalanceResponse balance = createBalance(
                List.of(
                        participant(1L, "Gabriel", 0L),
                        participant(2L, "Lucas", 0L)
                )
        );

        when(balanceService.calculateGroupBalance(1L))
                .thenReturn(balance);

        GroupSettlementResponse result =
                transferService.calculateSettlement(1L);

        assertTrue(result.transfers().isEmpty());
    }

    @Test
    void shouldHandleMultipleCreditorsAndDebtors() {
        GroupBalanceResponse balance = createBalance(
                List.of(
                        participant(1L, "Gabriel", 7000L),
                        participant(2L, "Lucas", 3000L),
                        participant(3L, "Ana", -4000L),
                        participant(4L, "Pedro", -6000L)
                )
        );

        when(balanceService.calculateGroupBalance(1L))
                .thenReturn(balance);

        GroupSettlementResponse result =
                transferService.calculateSettlement(1L);

        assertEquals(3, result.transfers().size());

        long totalTransferred = result.transfers().stream()
                .mapToLong(t -> t.amountInCents())
                .sum();

        assertEquals(10000L, totalTransferred);

        assertTrue(result.transfers().stream()
                .allMatch(t -> t.amountInCents() > 0));
    }

    private GroupBalanceResponse createBalance(
            List<ParticipantBalanceResponse> participants
    ) {
        return new GroupBalanceResponse(
                1L,
                "Beach Trip",
                0L,
                participants.size(),
                participants
        );
    }

    private ParticipantBalanceResponse participant(
            Long id,
            String name,
            Long balance
    ) {
        return new ParticipantBalanceResponse(
                id,
                name,
                0L,
                0L,
                balance
        );
    }
}