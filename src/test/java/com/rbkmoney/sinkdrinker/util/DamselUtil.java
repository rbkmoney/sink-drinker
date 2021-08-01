package com.rbkmoney.sinkdrinker.util;

import com.rbkmoney.damsel.domain.CashFlowAccount;
import com.rbkmoney.damsel.domain.CurrencyRef;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.MerchantCashFlowAccount;
import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rbkmoney.testcontainers.annotations.util.ThriftUtil.fillThriftObject;

public class DamselUtil {

    public static Event damselEvent(String payoutId, long eventId, PayoutChange payoutChange) {
        return new Event()
                .setId(eventId)
                .setCreatedAt(getCreatedAt())
                .setSource(EventSource.payout_id(payoutId))
                .setPayload(EventPayload.payout_changes(List.of(payoutChange)));
    }

    public static PayoutChange damselPayoutCreated(String payoutId) {
        return PayoutChange.payout_created(new PayoutCreated(damselPayout(payoutId)));
    }

    public static Payout damselPayout(String payoutId) {
        List<FinalCashFlowPosting> finalCashFlowPostings = IntStream.range(0, 2)
                .mapToObj(i -> fillThriftObject(new FinalCashFlowPosting(), FinalCashFlowPosting.class))
                .peek(finalCashFlowPosting -> {
                    finalCashFlowPosting.getSource().setAccountType(
                            CashFlowAccount.merchant(MerchantCashFlowAccount.settlement));
                    finalCashFlowPosting.getDestination().setAccountType(
                            CashFlowAccount.merchant(MerchantCashFlowAccount.payout));
                    finalCashFlowPosting.getVolume().setAmount(5L);
                })
                .collect(Collectors.toList());
        return new Payout()
                .setId(payoutId)
                .setPartyId(payoutId)
                .setShopId(payoutId)
                .setContractId(payoutId)
                .setCreatedAt(getCreatedAt())
                .setStatus(PayoutStatus.unpaid(new PayoutUnpaid()))
                .setAmount(1)
                .setFee(1)
                .setCurrency(new CurrencyRef("rub"))
                .setType(PayoutType.wallet(new Wallet(payoutId)))
                .setPayoutFlow(finalCashFlowPostings);
    }

    public static PayoutChange damselPayoutStatusPaid() {
        return PayoutChange.payout_status_changed(
                new PayoutStatusChanged(
                        PayoutStatus.paid(new PayoutPaid())));
    }

    public static String getCreatedAt() {
        return TypeUtil.temporalToString(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC));
    }

    public static com.rbkmoney.payout.manager.Payout toPayoutManagerPayout(
            com.rbkmoney.damsel.payout_processing.Payout damselPayout) {
        return new com.rbkmoney.payout.manager.Payout()
                .setPayoutId(damselPayout.getId())
                .setCreatedAt(damselPayout.getCreatedAt())
                .setPartyId(damselPayout.getPartyId())
                .setShopId(damselPayout.getShopId())
                .setStatus(com.rbkmoney.payout.manager.PayoutStatus.unpaid(
                        new com.rbkmoney.payout.manager.PayoutUnpaid()))
                .setCashFlow(damselPayout.getPayoutFlow())
                .setPayoutToolId(damselPayout.getId())
                .setAmount(damselPayout.getAmount())
                .setFee(damselPayout.getFee())
                .setCurrency(damselPayout.getCurrency());
    }

    public static String generatePayoutId() {
        return UUID.randomUUID().toString();
    }
}
