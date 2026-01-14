package com.example.factory.service;

import com.example.factory.dto.BatchResponse;
import com.example.factory.dto.EventRequest;
import com.example.factory.dto.MachineStatsResponse;
import com.example.factory.repository.EventRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventServiceTest {
    private static final Instant FIXED_EVENT_TIME = Instant.parse("2025-01-01T10:00:00Z");

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository repository;

    @AfterEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private EventRequest baseEvent(String eventId, int defectCount) {
        EventRequest e = new EventRequest();
        e.eventId = eventId;
        e.factoryId = "F01";
        e.lineId = "L01";
        e.machineId = "M-001";
        e.eventTime = FIXED_EVENT_TIME;
        e.durationMs = 1000;
        e.defectCount = defectCount;
        return e;
    }

    private EventRequest baseEvent(String eventId) {
        return baseEvent(eventId, 0);
    }

    @Test
    void newerDifferentPayloadUpdatesEvent() {
        EventRequest first = baseEvent("E-1");
        eventService.processBatch(List.of(first));

        EventRequest newer = baseEvent("E-1");
        newer.durationMs = 2000;

        BatchResponse response =
                eventService.processBatch(List.of(newer));

        assertThat(response.updated).isEqualTo(1);
        assertThat(repository.findById("E-1").orElseThrow()
                .getDurationMs()).isEqualTo(2000);
    }

    @Test
    void olderDifferentPayloadIgnored() {
        EventRequest first = baseEvent("E-1");
        eventService.processBatch(List.of(first));

        EventRequest second = baseEvent("E-1");
        second.durationMs = 3000;

        BatchResponse response =
                eventService.processBatch(List.of(second));


        assertThat(response.updated + response.deduped).isEqualTo(1);
    }

    @Test
    void invalidDurationRejected() {
        EventRequest bad = baseEvent("E-1");
        bad.durationMs = -5;

        BatchResponse response =
                eventService.processBatch(List.of(bad));

        assertThat(response.rejected).isEqualTo(1);
        assertThat(response.rejections.get(0).reason)
                .isEqualTo("INVALID_DURATION");
    }

    @Test
    void futureEventTimeRejected() {
        EventRequest bad = baseEvent("E-1");
        bad.eventTime = Instant.now().plusSeconds(3600);

        BatchResponse response =
                eventService.processBatch(List.of(bad));

        assertThat(response.rejected).isEqualTo(1);
        assertThat(response.rejections.get(0).reason)
                .isEqualTo("EVENT_TIME_IN_FUTURE");
    }

    @Test
    void defectMinusOneIgnoredInStats() {
        eventService.processBatch(List.of(
                baseEvent("E-1", -1),
                baseEvent("E-2", 4)
        ));

        MachineStatsResponse stats =
                eventService.getStats(
                        "M-001",
                        FIXED_EVENT_TIME.minusSeconds(10),
                        FIXED_EVENT_TIME.plusSeconds(10)
                );


        assertThat(stats.defectsCount).isEqualTo(4);
    }

    @Test
    void concurrentIngestionIsThreadSafe() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        executor.invokeAll(
                IntStream.range(0, 10)
                        .mapToObj(i -> (Runnable) () ->
                                eventService.processBatch(
                                        List.of(baseEvent("E-1"))
                                )
                        )
                        .map(Executors::callable)
                        .toList()
        );

        executor.shutdown();

        assertThat(repository.count()).isEqualTo(1);
    }


}
