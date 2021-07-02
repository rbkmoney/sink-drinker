package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SequenceForPayoutServiceTest extends AbstractDaoConfig {

    @Autowired
    private SequenceForPayoutService sequenceForPayoutService;

    @Test
    public void testFirstSequence() {
        String payoutId = generatePayoutId();
        sequenceForPayoutService.save(payoutId);
        assertEquals(0, sequenceForPayoutService.getSequenceId(payoutId));
    }

    @Test
    public void shouldNullWhenPayoutNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> sequenceForPayoutService.getSequenceId(generatePayoutId()));
    }

    @Test
    public void shouldIncrementSequence() {
        String payoutId = generatePayoutId();
        sequenceForPayoutService.save(payoutId);
        assertEquals(0, sequenceForPayoutService.getSequenceId(payoutId));
        sequenceForPayoutService.incrementSequence(payoutId);
        assertEquals(1, sequenceForPayoutService.getSequenceId(payoutId));
        sequenceForPayoutService.incrementSequence(payoutId);
        assertEquals(2, sequenceForPayoutService.getSequenceId(payoutId));
    }
}
