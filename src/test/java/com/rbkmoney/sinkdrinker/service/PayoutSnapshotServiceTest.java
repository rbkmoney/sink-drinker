package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.payout.manager.Payout;
import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.domain.PayoutSnapshot;
import com.rbkmoney.sinkdrinker.repository.PayoutSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PayoutSnapshotServiceTest extends AbstractDaoConfig {

    @Autowired
    private PayoutSnapshotService payoutSnapshotService;

    @Autowired
    private ThriftConverter thriftConverter;

    @Test
    public void shouldSaveAndGet() {
        String payoutId = generatePayoutId();
        when(partyManagementService.getPayoutToolId(anyString(), anyString())).thenReturn(payoutId);
        Payout payout = thriftConverter.toPayoutManagerPayout(damselPayout(payoutId));
        payoutSnapshotService.save(payout);
        Payout saved = payoutSnapshotService.get(payoutId);
        assertThat(saved).isEqualTo(payout);
    }
}
