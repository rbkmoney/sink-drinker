package com.rbkmoney.sinkdrinker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.json.JsonProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import com.rbkmoney.payout.manager.Payout;
import com.rbkmoney.sinkdrinker.domain.PayoutSnapshot;
import com.rbkmoney.sinkdrinker.repository.PayoutSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutSnapshotService {

    private final PayoutSnapshotRepository payoutSnapshotRepository;
    private final ObjectMapper objectMapper;
    private final TBaseProcessor thriftBaseProcessor;
    private final JsonProcessor jsonProcessor;

    public void save(Payout payout) {
        log.debug("Save snapshot payout={}", payout);
        String cashFlow = convertToJsonCashFlow(payout);
        String snapshot = convertToJsonPayoutSnapshot(payout);
        PayoutSnapshot payoutSnapshot = new PayoutSnapshot(payout.getPayoutId(), snapshot, cashFlow);
        payoutSnapshotRepository.save(payoutSnapshot);
    }

    public Optional<Payout> get(String payoutId) {
        log.debug("Get Payout from snapshot payoutId={}", payoutId);
        return payoutSnapshotRepository.findById(payoutId)
                .map(payoutSnapshot ->
                        convertToThriftPayout(payoutId, payoutSnapshot)
                                .setCashFlow(convertToThriftCashFlow(payoutId, payoutSnapshot)));
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

    private List<FinalCashFlowPosting> convertToThriftCashFlow(String payoutId, PayoutSnapshot payoutSnapshot) {
        try {
            var finalCashFlowPostings = new ArrayList<FinalCashFlowPosting>();
            for (JsonNode jsonNode : objectMapper.readTree(payoutSnapshot.getCashFlow())) {
                FinalCashFlowPosting finalCashFlowPosting = jsonToThriftBase(jsonNode, FinalCashFlowPosting.class);
                finalCashFlowPostings.add(finalCashFlowPosting);
            }
            return finalCashFlowPostings;
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Failed to map json content to CashFlow, payoutId='%s'", payoutId),
                    ex
            );
        }
    }

    private String convertToJsonPayoutSnapshot(Payout payout) {
        try {
            Payout copy = new Payout(payout);
            copy.setCashFlow(new ArrayList<>());
            JsonNode jsonNode = thriftBaseToJson(copy);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Failed to map cashFlows content to json, payout='%s'", payout),
                    ex
            );
        }
    }

    private String convertToJsonCashFlow(Payout payout) {
        try {
            var finalCashFlowPostings = payout.getCashFlow();
            var jsonNodes = new ArrayList<JsonNode>();
            for (FinalCashFlowPosting finalCashFlowPosting : finalCashFlowPostings) {
                JsonNode jsonNode = thriftBaseToJson(finalCashFlowPosting);
                jsonNodes.add(jsonNode);
            }
            return objectMapper.writeValueAsString(jsonNodes);
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Failed to map cashFlows content to json, payout='%s'", payout),
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
