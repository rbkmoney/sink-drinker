package com.rbkmoney.sinkdrinker.repository;

import com.rbkmoney.sinkdrinker.domain.PayoutSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutSnapshotRepository extends JpaRepository<PayoutSnapshot, String> {

}
