package com.rbkmoney.sinkdrinker.repository;

import com.rbkmoney.sinkdrinker.domain.PayoutSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PayoutSnapshotRepository extends JpaRepository<PayoutSnapshot, String> {

    @Transactional
    @Modifying
    @Query("UPDATE PayoutSnapshot p SET p.sequenceId = p.sequenceId + 1 WHERE p.payoutId = ?1")
    void incrementSequence(String payoutId);

}
