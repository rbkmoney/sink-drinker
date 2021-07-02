package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.sinkdrinker.domain.SequenceForPayout;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import com.rbkmoney.sinkdrinker.repository.SequenceForPayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SequenceForPayoutService {

    private final SequenceForPayoutRepository sequenceForPayoutRepository;

    public void save(String payoutId) {
        log.debug("Save first SequenceId payoutId={}", payoutId);
        SequenceForPayout sequenceForPayout = new SequenceForPayout(payoutId, 0);
        sequenceForPayoutRepository.save(sequenceForPayout);
    }

    public void incrementSequence(String payoutId) {
        log.debug("Increment SequenceId payoutId={}", payoutId);
        sequenceForPayoutRepository.incrementSequence(payoutId);
    }

    public Integer getSequenceId(String payoutId) {
        log.debug("Get SequenceId payoutId={}", payoutId);
        return sequenceForPayoutRepository.findById(payoutId)
                .map(SequenceForPayout::getSequenceId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("SequenceId is null with payoutId=%s", payoutId)));
    }
}
