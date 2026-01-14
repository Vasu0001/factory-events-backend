package com.example.factory.repository;

import com.example.factory.entity.EventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest {

    @Autowired
    private EventRepository repository;

    @Test
    void saveAndFetchEvent_byId() {
        EventEntity event = new EventEntity(
                "E-1",
                Instant.parse("2026-01-15T10:00:00Z"),
                Instant.now(),
                "F01",
                "L01",
                "M-001",
                1000L,
                0,
                "hash123"
        );

        repository.save(event);

        Optional<EventEntity> saved = repository.findById("E-1");

        assertThat(saved).isPresent();
        assertThat(saved.get().getFactoryId()).isEqualTo("F01");
        assertThat(saved.get().getLineId()).isEqualTo("L01");
        assertThat(saved.get().getMachineId()).isEqualTo("M-001");
        assertThat(saved.get().getDurationMs()).isEqualTo(1000L);
    }

    @Test
    void startInclusive_endExclusive_timeWindow() {
        Instant start = Instant.parse("2026-01-15T00:00:00Z");
        Instant end = start.plusSeconds(3600);

        EventEntity atStart = new EventEntity(
                "E-START",
                start,
                Instant.now(),
                "F01",
                "L01",
                "M-001",
                1000L,
                1,
                "hashA"
        );

        EventEntity atEnd = new EventEntity(
                "E-END",
                end,
                Instant.now(),
                "F01",
                "L01",
                "M-001",
                1000L,
                1,
                "hashB"
        );

        repository.save(atStart);
        repository.save(atEnd);

        List<EventEntity> results =
                repository.findByMachineIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                        "M-001",
                        start,
                        end
                );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEventId()).isEqualTo("E-START");
    }
}
