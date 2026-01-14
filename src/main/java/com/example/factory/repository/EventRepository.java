package com.example.factory.repository;
import com.example.factory.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
@Repository
public interface EventRepository
        extends JpaRepository<EventEntity, String> {
    List<EventEntity> findByMachineIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            String machineId,
            Instant start,
            Instant end
    );
    List<EventEntity> findByFactoryIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            String factoryId,
            Instant from,
            Instant to
    );
    List<EventEntity> findByFactoryIdAndEventTimeBetween(
            String factoryId,
            Instant from,
            Instant to
    );

    List<EventEntity> findByFactoryIdAndLineIdAndEventTimeBetween(
            String factoryId,
            String lineId,
            Instant from,
            Instant to
    );
    List<EventEntity> findByMachineIdAndEventTimeBetween(
            String machineId,
            Instant start,
            Instant end
    );

}
