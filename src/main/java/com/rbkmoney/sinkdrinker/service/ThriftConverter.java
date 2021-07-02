package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.payout.manager.*;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThriftConverter {

    private final PartyManagementService partyManagementService;
    private final SequenceForPayoutService sequenceForPayoutService;
    private final PayoutSnapshotService payoutSnapshotService;

    public List<Event> createEvents(
            com.rbkmoney.damsel.payout_processing.Event damselEvent,
            String payoutId) {
        return damselEvent.getPayload().getPayoutChanges().stream()
                .map(payoutChange -> toPayoutManagerPayoutChange(payoutChange, payoutId))
                .map(payoutChange -> new Event()
                        .setPayoutId(payoutId)
                        .setSequenceId(getSequenceId(payoutChange, payoutId))
                        .setCreatedAt(damselEvent.getCreatedAt())
                        .setPayoutChange(payoutChange)
                        .setPayout(payoutSnapshotService.get(payoutId)))
                .collect(Collectors.toList());
    }

    public Payout toPayoutManagerPayout(
            com.rbkmoney.damsel.payout_processing.Payout damselPayout) {
        String payoutToolId = partyManagementService.getPayoutToolId(
                damselPayout.getPartyId(),
                damselPayout.getShopId());
        return new Payout()
                .setPayoutId(damselPayout.getId())
                .setCreatedAt(damselPayout.getCreatedAt())
                .setPartyId(damselPayout.getPartyId())
                .setShopId(damselPayout.getShopId())
                .setStatus(toPayoutManagerPayoutStatus(damselPayout.getStatus()))
                .setCashFlow(damselPayout.getPayoutFlow())
                .setPayoutToolId(payoutToolId)
                .setAmount(damselPayout.getAmount())
                .setFee(damselPayout.getFee())
                .setCurrency(damselPayout.getCurrency());
    }

    private Integer getSequenceId(PayoutChange payoutChange, String payoutId) {
        switch (payoutChange.getSetField()) {
            case CREATED:
                sequenceForPayoutService.save(payoutId);
                return sequenceForPayoutService.getSequenceId(payoutId);
            case STATUS_CHANGED:
                sequenceForPayoutService.incrementSequence(payoutId);
                return sequenceForPayoutService.getSequenceId(payoutId);
            default:
                throw new NotFoundException(String.format("Payout change not found, change = %s", payoutChange));
        }
    }

    private PayoutChange toPayoutManagerPayoutChange(
            com.rbkmoney.damsel.payout_processing.PayoutChange damselPayoutChange,
            String payoutId) {
        switch (damselPayoutChange.getSetField()) {
            case PAYOUT_CREATED: {
                Payout payout = toPayoutManagerPayout(damselPayoutChange.getPayoutCreated().getPayout());
                payoutSnapshotService.save(payout);
                return PayoutChange.created(new PayoutCreated(payout));
            }
            case PAYOUT_STATUS_CHANGED: {
                PayoutStatus status = toPayoutManagerPayoutStatus(
                        damselPayoutChange.getPayoutStatusChanged().getStatus());
                Payout payout = payoutSnapshotService.get(payoutId);
                payout.setStatus(status);
                payoutSnapshotService.save(payout);
                return PayoutChange.status_changed(new PayoutStatusChanged(status));
            }
            default:
                throw new NotFoundException(String.format("Payout change not found, change = %s", damselPayoutChange));
        }
    }

    private PayoutStatus toPayoutManagerPayoutStatus(
            com.rbkmoney.damsel.payout_processing.PayoutStatus damselPayoutStatus) {
        switch (damselPayoutStatus.getSetField()) {
            case UNPAID:
                return PayoutStatus.unpaid(new PayoutUnpaid());
            case PAID:
                return PayoutStatus.paid(new PayoutPaid());
            case CONFIRMED:
                return PayoutStatus.confirmed(new PayoutConfirmed());
            case CANCELLED:
                return PayoutStatus.cancelled(new PayoutCancelled(damselPayoutStatus.getCancelled().getDetails()));
            default:
                throw new NotFoundException(String.format("Payout status not found, status = %s", damselPayoutStatus));
        }
    }
}
