package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.payout.manager.*;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ThriftUtil {

    public static List<Event> createEvents(
            com.rbkmoney.damsel.payout_processing.Event event,
            String payoutId,
            BiFunction<String, Boolean, Integer> sequenceId,
            BiFunction<String, String, String> payoutToolId) {
        return event.getPayload().getPayoutChanges().stream()
                .map(payoutChange -> toPayoutManagerPayoutChange(payoutChange, payoutToolId))
                .map(payoutChange -> new Event()
                        .setPayoutId(payoutId)
                        .setSequenceId(getSequenceId(sequenceId, payoutId, payoutChange))
                        .setCreatedAt(event.getCreatedAt())
                        .setPayoutChange(payoutChange))
                .collect(Collectors.toList());
    }

    private static int getSequenceId(
            BiFunction<String, Boolean, Integer> sequenceId,
            String payoutId,
            PayoutChange payoutChange) {
        switch (payoutChange.getSetField()) {
            case CREATED:
                return sequenceId.apply(payoutId, true);
            case STATUS_CHANGED:
                return sequenceId.apply(payoutId, false);
            default:
                throw new NotFoundException(String.format("Payout change not found, change = %s", payoutChange));
        }
    }

    private static PayoutChange toPayoutManagerPayoutChange(
            com.rbkmoney.damsel.payout_processing.PayoutChange payoutChange,
            BiFunction<String, String, String> payoutToolId) {
        switch (payoutChange.getSetField()) {
            case PAYOUT_CREATED:
                return PayoutChange.created(
                        new PayoutCreated(
                                toPayoutManagerPayout(payoutChange.getPayoutCreated().getPayout(), payoutToolId)));
            case PAYOUT_STATUS_CHANGED:
                return PayoutChange.status_changed(
                        new PayoutStatusChanged(
                                toPayoutManagerPayoutStatus(payoutChange.getPayoutStatusChanged().getStatus())));
            default:
                throw new NotFoundException(String.format("Payout change not found, change = %s", payoutChange));
        }
    }

    private static Payout toPayoutManagerPayout(
            com.rbkmoney.damsel.payout_processing.Payout payout,
            BiFunction<String, String, String> payoutToolId) {
        return new Payout()
                .setPayoutId(payout.getId())
                .setCreatedAt(payout.getCreatedAt())
                .setPartyId(payout.getPartyId())
                .setShopId(payout.getShopId())
                .setStatus(toPayoutManagerPayoutStatus(payout.getStatus()))
                .setCashFlow(payout.getPayoutFlow())
                .setPayoutToolId(payoutToolId.apply(payout.getPartyId(), payout.getShopId()))
                .setAmount(payout.getAmount())
                .setFee(payout.getFee())
                .setCurrency(payout.getCurrency());
    }

    private static PayoutStatus toPayoutManagerPayoutStatus(
            com.rbkmoney.damsel.payout_processing.PayoutStatus payoutStatus) {
        switch (payoutStatus.getSetField()) {
            case UNPAID:
                return PayoutStatus.unpaid(new PayoutUnpaid());
            case PAID:
                return PayoutStatus.paid(new PayoutPaid());
            case CONFIRMED:
                return PayoutStatus.confirmed(new PayoutConfirmed());
            case CANCELLED:
                return PayoutStatus.cancelled(new PayoutCancelled(payoutStatus.getCancelled().getDetails()));
            default:
                throw new NotFoundException(String.format("Payout status not found, status = %s", payoutStatus));
        }
    }
}
