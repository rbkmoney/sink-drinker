package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.domain.SequenceForPayout;
import com.rbkmoney.sinkdrinker.repository.SequenceForPayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SequenceForPayoutServiceTest extends AbstractDaoConfig {

    @Autowired
    private SequenceForPayoutService sequenceForPayoutService;

    @Test
    public void testFirstSequence() {
        assertEquals(
                0,
                sequenceForPayoutService.getSequenceId(generatePayoutId(), true));
    }

    @Test
    public void shouldNullWhenPayoutNotFound() {
        assertNull(sequenceForPayoutService.getSequenceId(generatePayoutId(), false));
    }

    @Test
    public void shouldIncrementSequence() {
        String payoutId = generatePayoutId();
        sequenceForPayoutService.getSequenceId(payoutId, true);
        assertEquals(
                1,
                sequenceForPayoutService.getSequenceId(payoutId, false));
        assertEquals(
                2,
                sequenceForPayoutService.getSequenceId(payoutId, false));
    }
}
