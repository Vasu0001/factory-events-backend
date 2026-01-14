package com.example.factory.service;

import com.example.factory.dto.*;
import com.example.factory.entity.EventEntity;
import com.example.factory.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository repository;

    private static final long MAX_DURATION_MS = 6L * 60 * 60 * 1000;
    private static final long MAX_FUTURE_SECONDS = 15 * 60;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BatchResponse processBatch(List<EventRequest> requests) {

        BatchResponse response = new BatchResponse();

        // Implemented a map to process large number of events
        Map<String, EventRequest> resolved = new HashMap<>();

        for (EventRequest req : requests) {

            String error = validate(req);
            if (error != null) {
                response.rejected++;
                response.rejections.add(
                        new BatchResponse.Rejection(req.eventId, error)
                );
                continue;
            }

            EventRequest prev = resolved.get(req.eventId);

            if (prev == null) {
                resolved.put(req.eventId, req);
                continue;
            }

            String prevHash = computePayloadHash(prev);
            String currHash = computePayloadHash(req);

            if (prevHash.equals(currHash)
                    && prev.eventTime.equals(req.eventTime)) {
                response.deduped++;
                continue;
            }

            response.updated++;
            resolved.put(req.eventId, req);
        }

        Instant now = Instant.now();
        // Hashmap has the reduced number of events , which we either directly want to add to DB
        // or want to check for there presence in DB
        for (EventRequest req : resolved.values()) {

            EventEntity existing =
                    repository.findById(req.eventId).orElse(null);

            String payloadHash = computePayloadHash(req);

            if (existing == null) {
                repository.save(new EventEntity(
                        req.eventId,
                        req.eventTime,
                        now,
                        req.factoryId,
                        req.lineId,
                        req.machineId,
                        req.durationMs,
                        req.defectCount,
                        payloadHash
                ));
                response.accepted++;
                continue;
            }

            if (!existing.getPayloadHash().equals(payloadHash)) {

                if (now.isAfter(existing.getReceivedTime())) {
                    existing.setEventTime(req.eventTime);
                    existing.setFactoryId(req.factoryId);
                    existing.setLineId(req.lineId);
                    existing.setMachineId(req.machineId);
                    existing.setDurationMs(req.durationMs);
                    existing.setDefectCount(req.defectCount);
                    existing.setPayloadHash(payloadHash);
                    existing.setReceivedTime(now);

                    repository.save(existing);
                    response.updated++;
                } else {
                    response.ignored++;
                }

            } else {
                response.deduped++;
            }
        }

        return response;
    }
    private String validate(EventRequest req) {

        if (req.durationMs < 0 || req.durationMs > MAX_DURATION_MS) {
            return "INVALID_DURATION";
        }

        if (req.eventTime.isAfter(
                Instant.now().plusSeconds(MAX_FUTURE_SECONDS))) {
            return "EVENT_TIME_IN_FUTURE";
        }

        return null;
    }
    private String computePayloadHash(EventRequest req) {
        String payload =
                req.eventId + "|" +
                        req.factoryId + "|" +
                        req.lineId + "|" +
                        req.machineId + "|" +
                        req.durationMs + "|" +
                        req.defectCount;

        return Integer.toHexString(payload.hashCode());
    }
    public MachineStatsResponse getStats(
            String machineId,
            Instant start,
            Instant end
    ) {

        List<EventEntity> events =
                repository.findByMachineIdAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                        machineId, start, end
                );

        long eventsCount = events.size();

        long defectsCount = events.stream()
                .filter(e -> e.getDefectCount() >= 0)
                .mapToLong(EventEntity::getDefectCount)
                .sum();

        double windowHours =
                (end.getEpochSecond() - start.getEpochSecond()) / 3600.0;

        double avgDefectRate =
                windowHours <= 0 ? 0.0 : defectsCount / windowHours;

        String status =
                avgDefectRate < 2.0 ? "Healthy" : "Warning";

        return new MachineStatsResponse(
                machineId,
                start,
                end,
                eventsCount,
                defectsCount,
                avgDefectRate,
                status
        );
    }

    public MachineStatsResponse getMachineStats(
            String machineId,
            Instant start,
            Instant end
    ) {
        return getStats(machineId, start, end);
    }
    public List<TopDefectLineResponse> getTopDefectLines(
            String factoryId,
            Instant from,
            Instant to,
            int limit
    ) {

        List<EventEntity> events =
                repository.findByFactoryIdAndEventTimeBetween(factoryId, from, to);

        Map<String, List<EventEntity>> byLine =
                events.stream()
                        .collect(Collectors.groupingBy(EventEntity::getLineId));

        List<TopDefectLineResponse> result = new ArrayList<>();

        for (Map.Entry<String, List<EventEntity>> entry : byLine.entrySet()) {

            long totalDefects = entry.getValue().stream()
                    .filter(e -> e.getDefectCount() >= 0)
                    .mapToLong(EventEntity::getDefectCount)
                    .sum();

            long eventCount = entry.getValue().size();

            double defectPercent =
                    eventCount == 0 ? 0.0 :
                            Math.round((totalDefects * 10000.0 / eventCount)) / 100.0;

            result.add(new TopDefectLineResponse(
                    entry.getKey(),
                    totalDefects,
                    eventCount,
                    defectPercent
            ));
        }

        return result.stream()
                .sorted(Comparator.comparingLong(
                        TopDefectLineResponse::getTotalDefects).reversed())
                .limit(limit)
                .toList();
    }
}
