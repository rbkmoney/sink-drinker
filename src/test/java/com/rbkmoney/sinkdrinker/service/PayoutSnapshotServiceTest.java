package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.payout.manager.Payout;
import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.domain.PayoutSnapshot;
import com.rbkmoney.sinkdrinker.repository.PayoutSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PayoutSnapshotServiceTest extends AbstractDaoConfig {

    @Autowired
    private PayoutSnapshotRepository payoutSnapshotRepository;

    @Autowired
    private PayoutSnapshotService payoutSnapshotService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        PayoutSnapshot payoutSnapshot = new PayoutSnapshot("trap", "trap", "trap");
        payoutSnapshotRepository.save(payoutSnapshot);
    }

    @Test
    public void name() {
        String payoutId = generatePayoutId();
        Payout payout = ThriftUtil.toPayoutManagerPayout(damselPayout(payoutId), (partyId, shopId) -> payoutId);
        payoutSnapshotService.save(payout);

        Optional<Payout> saved = payoutSnapshotService.get(payoutId);
        assertThat(saved).isPresent();
        assertThat(saved.get()).isEqualTo(payout);
    }
}
