package com.rbkmoney.sinkdrinker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.json.JsonProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import com.rbkmoney.payout.manager.Payout;
import com.rbkmoney.sinkdrinker.domain.PayoutSnapshot;
import com.rbkmoney.sinkdrinker.dto.PayoutData;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import com.rbkmoney.sinkdrinker.repository.PayoutSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutSnapshotService {

    private final PayoutSnapshotRepository payoutSnapshotRepository;
    private final ObjectMapper objectMapper;
    private final TBaseProcessor thriftBaseProcessor;
    private final JsonProcessor jsonProcessor;

    public void save(Payout payout, Integer sequenceId) {
        log.debug("Save snapshot, payout={}", payout);
        String snapshot = convertToJsonPayoutSnapshot(payout);
        PayoutSnapshot payoutSnapshot = new PayoutSnapshot(payout.getPayoutId(), snapshot, sequenceId);
        payoutSnapshotRepository.save(payoutSnapshot);
    }

    public PayoutData get(String payoutId) {
        log.debug("Get Payout from snapshot payoutId={}", payoutId);
        return payoutSnapshotRepository.findById(payoutId)
                .map(payoutSnapshot -> new PayoutData(
                        null,
                        convertToThriftPayout(payoutId, payoutSnapshot),
                        payoutSnapshot.getSequenceId()))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Snapshot is null with payoutId=%s", payoutId)));
    }

    private Payout convertToThriftPayout(String payoutId, PayoutSnapshot payoutSnapshot) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payoutSnapshot.getSnapshot());
            return jsonToThriftBase(jsonNode, Payout.class);
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Failed to map json content to Payout, payoutId='%s'", payoutId),
                    ex
            );
        }
    }

    private String convertToJsonPayoutSnapshot(Payout payout) {
        try {
            return objectMapper.writeValueAsString(thriftBaseToJson(new Payout(payout)));
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Failed to map Payout content to json, payout='%s'", payout),
                    ex
            );
        }
    }

    private <T extends TBase> JsonNode thriftBaseToJson(T thriftBase) throws IOException {
        return thriftBaseProcessor.process(thriftBase, new JsonHandler());
    }

    private <T extends TBase> T jsonToThriftBase(JsonNode jsonNode, Class<T> type) throws IOException {
        return jsonProcessor.process(jsonNode, new TBaseHandler<>(type));
    }
}
