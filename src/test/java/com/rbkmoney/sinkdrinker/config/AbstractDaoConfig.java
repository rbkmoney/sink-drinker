package com.rbkmoney.sinkdrinker.config;

import com.rbkmoney.damsel.domain.CashFlowAccount;
import com.rbkmoney.damsel.domain.CurrencyRef;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.damsel.domain.MerchantCashFlowAccount;
import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.sinkdrinker.SinkDrinkerApplication;
import com.rbkmoney.sinkdrinker.service.PartyManagementService;
import lombok.SneakyThrows;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = SinkDrinkerApplication.class,
        initializers = AbstractDaoConfig.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource("classpath:application.yml")
@Testcontainers
public abstract class AbstractDaoConfig {

    @Container
    public static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:9.6"));

    @LocalServerPort
    protected int port;

    @MockBean
    protected PartyManagementService partyManagementService;

    private MockTBaseProcessor mockTBaseProcessor;

    @BeforeEach
    public void setUp() {
        mockTBaseProcessor = new MockTBaseProcessor(MockMode.ALL, 20, 1);
        mockTBaseProcessor.addFieldHandler(
                structHandler -> structHandler.value(Instant.now().toString()),
                "created_at", "at", "due");
    }

    public static class Initializer extends ConfigDataApplicationContextInitializer {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "flyway.url=" + postgres.getJdbcUrl(),
                    "flyway.user=" + postgres.getUsername(),
                    "flyway.password=" + postgres.getPassword(),
                    "polling.enabled=false")
                    .applyTo(configurableApplicationContext);
        }
    }

    public Event damselEvent(String payoutId, long eventId, PayoutChange payoutChange) {
        return new Event()
                .setId(eventId)
                .setCreatedAt(getCreatedAt())
                .setSource(EventSource.payout_id(payoutId))
                .setPayload(EventPayload.payout_changes(List.of(payoutChange)));
    }

    public PayoutChange damselPayoutCreated(String payoutId) {
        return PayoutChange.payout_created(new PayoutCreated(damselPayout(payoutId)));
    }

    public Payout damselPayout(String payoutId) {
        List<FinalCashFlowPosting> finalCashFlowPostings = IntStream.range(0, 2)
                .mapToObj(i -> fillThrift(new FinalCashFlowPosting(), FinalCashFlowPosting.class))
                .peek(finalCashFlowPosting -> {
                    finalCashFlowPosting.getSource().setAccountType(
                            CashFlowAccount.merchant(MerchantCashFlowAccount.settlement));
                    finalCashFlowPosting.getDestination().setAccountType(
                            CashFlowAccount.merchant(MerchantCashFlowAccount.payout));
                    finalCashFlowPosting.getVolume().setAmount(5L);
                })
                .collect(Collectors.toList());
        return new Payout()
                .setId(payoutId)
                .setPartyId(payoutId)
                .setShopId(payoutId)
                .setContractId(payoutId)
                .setCreatedAt(getCreatedAt())
                .setStatus(PayoutStatus.unpaid(new PayoutUnpaid()))
                .setAmount(1)
                .setFee(1)
                .setCurrency(new CurrencyRef("rub"))
                .setType(PayoutType.wallet(new Wallet(payoutId)))
                .setPayoutFlow(finalCashFlowPostings);
    }

    public PayoutChange damselPayoutStatusPaid() {
        return PayoutChange.payout_status_changed(
                new PayoutStatusChanged(
                        PayoutStatus.paid(new PayoutPaid())));
    }

    public String getCreatedAt() {
        return TypeUtil.temporalToString(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC));
    }

    public String generatePayoutId() {
        return UUID.randomUUID().toString();
    }

    @SneakyThrows
    public <T extends TBase> T fillThrift(T value, Class<T> type) {
        return mockTBaseProcessor.process(value, new TBaseHandler<>(type));
    }
}
