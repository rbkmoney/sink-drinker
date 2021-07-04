package com.rbkmoney.sinkdrinker.dto;

import com.rbkmoney.payout.manager.Payout;
import com.rbkmoney.payout.manager.PayoutChange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayoutData {

    @Nullable
    private PayoutChange payoutChange;
    private Payout payout;
    private Integer sequenceId;

}
