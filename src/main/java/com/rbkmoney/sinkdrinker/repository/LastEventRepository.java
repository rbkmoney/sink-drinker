package com.rbkmoney.sinkdrinker.repository;

import com.rbkmoney.sinkdrinker.domain.LastEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastEventRepository extends JpaRepository<LastEvent, String> {

}
