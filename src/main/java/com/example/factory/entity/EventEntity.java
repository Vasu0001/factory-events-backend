package com.example.factory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "received_time", nullable = false)
    private Instant receivedTime;

    @Column(name = "machine_id", nullable = false)
    private String machineId;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "defect_count", nullable = false)
    private int defectCount;

    @Column(name = "payload_hash", nullable = false)
    private String payloadHash;
    @Column(nullable = false)
    private String factoryId;

    @Column(nullable = false)
    private String lineId;


    protected EventEntity() {}

    public EventEntity(
            String eventId,
            Instant eventTime,
            Instant receivedTime,
            String factoryId,
            String lineId,
            String machineId,
            long durationMs,
            int defectCount,
            String payloadHash
    ) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.receivedTime = receivedTime;
        this.factoryId = factoryId;
        this.lineId = lineId;
        this.machineId = machineId;
        this.durationMs = durationMs;
        this.defectCount = defectCount;
        this.payloadHash = payloadHash;
    }


    public String getEventId() { return eventId; }
    public Instant getEventTime() { return eventTime; }
    public Instant getReceivedTime() { return receivedTime; }
    public String getMachineId() { return machineId; }
    public long getDurationMs() { return durationMs; }
    public int getDefectCount() { return defectCount; }
    public String getPayloadHash() { return payloadHash; }
    public String getFactoryId() { return factoryId; }

    public String getLineId() { return lineId; }

    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }
    public void setReceivedTime(Instant receivedTime) { this.receivedTime = receivedTime; }
    public void setFactoryId(String factoryId) { this.factoryId = factoryId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public void setDefectCount(int defectCount) { this.defectCount = defectCount; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
}