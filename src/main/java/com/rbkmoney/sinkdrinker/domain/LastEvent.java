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
@Table(name = "last_event")
public class LastEvent implements Serializable {

    @Id
    @Column(name = "sink_id")
    private String sinkId;

    @Column(name = "id")
    private Long id;
}
