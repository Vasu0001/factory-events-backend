package com.example.factory.dto;

import java.time.Instant;

public class MachineStatsResponse {

    public String machineId;
    public Instant start;
    public Instant end;
    public long eventsCount;
    public long defectsCount;
    public double avgDefectRate;
    public String status;

    public MachineStatsResponse(
            String machineId,
            Instant start,
            Instant end,
            long eventsCount,
            long defectsCount,
            double avgDefectRate,
            String status
    ) {
        this.machineId = machineId;
        this.start = start;
        this.end = end;
        this.eventsCount = eventsCount;
        this.defectsCount = defectsCount;
        this.avgDefectRate = avgDefectRate;
        this.status = status;
    }
}
