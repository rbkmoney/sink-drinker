package com.rbkmoney.sinkdrinker.repository;

import com.rbkmoney.sinkdrinker.config.AbstractDaoConfig;
import com.rbkmoney.sinkdrinker.domain.SequenceForPayout;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SequenceForPayoutRepositoryTest extends AbstractDaoConfig {

    @Autowired
    private SequenceForPayoutRepository sequenceForPayoutRepository;

    @Before
    public void setUp() {
        SequenceForPayout trap = new SequenceForPayout("trap", 0);
        sequenceForPayoutRepository.save(trap);
    }

    @Test
    public void shouldSaveAndFindWhenUpdate() {
        String payoutId = generatePayoutId();
        SequenceForPayout sequenceForPayout = new SequenceForPayout(payoutId, 0);
        sequenceForPayoutRepository.save(sequenceForPayout);
        Optional<SequenceForPayout> saved = sequenceForPayoutRepository.findById(payoutId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSequenceId()).isEqualTo(0);
        sequenceForPayout.setSequenceId(10);
        sequenceForPayoutRepository.save(sequenceForPayout);
        saved = sequenceForPayoutRepository.findById(payoutId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSequenceId()).isEqualTo(10);
    }

    @Test
    public void shouldIncrementAndFind() {
        String payoutId = generatePayoutId();
        SequenceForPayout sequenceForPayout = new SequenceForPayout(payoutId, 0);
        sequenceForPayoutRepository.save(sequenceForPayout);
        Optional<SequenceForPayout> saved = sequenceForPayoutRepository.findById(payoutId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSequenceId()).isEqualTo(0);
        sequenceForPayoutRepository.incrementSequence(payoutId);
        saved = sequenceForPayoutRepository.findById(payoutId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getSequenceId()).isEqualTo(1);
    }

    @Test
    public void shouldNotThrowWhenUpdateDoesNotExist() {
        String payoutId = "payoutId";
        sequenceForPayoutRepository.incrementSequence(payoutId);
        Optional<SequenceForPayout> saved = sequenceForPayoutRepository.findById(payoutId);
        assertThat(saved).isEmpty();
    }
}
