package com.rbkmoney.sinkdrinker.repository;

import com.rbkmoney.sinkdrinker.domain.SequenceForPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SequenceForPayoutRepository extends JpaRepository<SequenceForPayout, String> {

    @Transactional
    @Modifying
    @Query("UPDATE SequenceForPayout sfp SET sfp.sequenceId = sfp.sequenceId + 1 WHERE sfp.payoutId = ?1")
    void incrementSequence(String payoutId);

}
