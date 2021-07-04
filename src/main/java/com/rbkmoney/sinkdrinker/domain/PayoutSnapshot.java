package com.rbkmoney.sinkdrinker.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payout_snapshot")
public class PayoutSnapshot implements Serializable {

    @Id
    @Column(name = "payout_id")
    private String payoutId;

    @Column(name = "snapshot")
    private String snapshot;

    @Column(name = "sequence_id")
    private Integer sequenceId;

}
