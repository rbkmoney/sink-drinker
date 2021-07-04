package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.payout.manager.*;
import com.rbkmoney.sinkdrinker.dto.PayoutData;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThriftEventsService {

    private final PartyManagementService partyManagementService;
    private final PayoutSnapshotService payoutSnapshotService;

    public List<Event> createEvents(
            com.rbkmoney.damsel.payout_processing.Event damselEvent,
            String payoutId) {
        return damselEvent.getPayload().getPayoutChanges().stream()
                .map(damselPayoutChange -> toPayoutManagerPayoutChange(damselPayoutChange, payoutId))
                .map(payoutData -> new Event()
                        .setPayoutId(payoutId)
                        .setSequenceId(payoutData.getSequenceId())
                        .setCreatedAt(damselEvent.getCreatedAt())
                        .setPayoutChange(payoutData.getPayoutChange())
                        .setPayout(payoutData.getPayout()))
                .collect(Collectors.toList());
    }

    private PayoutData toPayoutManagerPayoutChange(
            com.rbkmoney.damsel.payout_processing.PayoutChange damselPayoutChange,
            String payoutId) {
        switch (damselPayoutChange.getSetField()) {
            case PAYOUT_CREATED: {
                Payout payout = toPayoutManagerPayout(damselPayoutChange.getPayoutCreated().getPayout());
                int sequenceId = 0;
                payoutSnapshotService.save(payout, sequenceId);
                return new PayoutData(PayoutChange.created(new PayoutCreated(payout)), payout, sequenceId);
            }
            case PAYOUT_STATUS_CHANGED: {
                PayoutStatus status = toPayoutManagerPayoutStatus(
                        damselPayoutChange.getPayoutStatusChanged().getStatus());
                PayoutData payoutData = payoutSnapshotService.get(payoutId);
                Payout payout = payoutData.getPayout();
                payout.setStatus(status);
                int incrementedSequenceId = payoutData.getSequenceId() + 1;
                payoutSnapshotService.save(payout, incrementedSequenceId);
                return new PayoutData(
                        PayoutChange.status_changed(new PayoutStatusChanged(status)),
                        payout,
                        incrementedSequenceId);
            }
            default:
                throw new NotFoundException(String.format("Payout change not found, change = %s", damselPayoutChange));
        }
    }

    private Payout toPayoutManagerPayout(
            com.rbkmoney.damsel.payout_processing.Payout damselPayout) {
        return new Payout()
                .setPayoutId(damselPayout.getId())
                .setCreatedAt(damselPayout.getCreatedAt())
                .setPartyId(damselPayout.getPartyId())
                .setShopId(damselPayout.getShopId())
                .setStatus(toPayoutManagerPayoutStatus(damselPayout.getStatus()))
                .setCashFlow(damselPayout.getPayoutFlow())
                .setPayoutToolId(getPayoutToolId(damselPayout))
                .setAmount(damselPayout.getAmount())
                .setFee(damselPayout.getFee())
                .setCurrency(damselPayout.getCurrency());
    }

    private String getPayoutToolId(com.rbkmoney.damsel.payout_processing.Payout damselPayout) {
        return partyManagementService.getPayoutToolId(damselPayout.getPartyId(), damselPayout.getShopId());
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
