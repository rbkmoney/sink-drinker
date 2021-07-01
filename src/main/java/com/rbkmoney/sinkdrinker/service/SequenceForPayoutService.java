package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.sinkdrinker.domain.SequenceForPayout;
import com.rbkmoney.sinkdrinker.repository.SequenceForPayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SequenceForPayoutService {

    private final SequenceForPayoutRepository sequenceForPayoutRepository;

    public Integer getSequenceId(String payoutId, Boolean isCreatedEvent) {
        if (isCreatedEvent) {
            SequenceForPayout sequenceForPayout = new SequenceForPayout(payoutId, 0);
            sequenceForPayoutRepository.save(sequenceForPayout);
        } else {
            sequenceForPayoutRepository.incrementSequence(payoutId);
        }
        return getSequenceId(payoutId);
    }

    private Integer getSequenceId(String payoutId) {
        return sequenceForPayoutRepository.findById(payoutId)
                .map(SequenceForPayout::getSequenceId)
                .orElse(null);
    }
}