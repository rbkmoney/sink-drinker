package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.payout.manager.Payout;
import com.rbkmoney.sinkdrinker.config.PostgresqlSpringBootITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.rbkmoney.sinkdrinker.util.DamselUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@PostgresqlSpringBootITest
public class PayoutSnapshotServiceTest {

    @Autowired
    private PayoutSnapshotService payoutSnapshotService;

    @Test
    public void shouldSaveAndGet() {
        String payoutId = generatePayoutId();
        Payout payout = toPayoutManagerPayout(damselPayout(payoutId));
        payoutSnapshotService.save(payout, 0);
        Payout saved = payoutSnapshotService.get(payoutId).getPayout();
        assertThat(saved).isEqualTo(payout);
    }
}
