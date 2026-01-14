package com.example.factory.dto;

public class TopDefectLineResponse {

    public String lineId;
    public long totalDefects;
    public long eventCount;
    public double defectsPercent;

    public TopDefectLineResponse(
            String lineId,
            long totalDefects,
            long eventCount,
            double defectsPercent
    ) {
        this.lineId = lineId;
        this.totalDefects = totalDefects;
        this.eventCount = eventCount;
        this.defectsPercent = defectsPercent;
    }
    public long getTotalDefects() {
        return totalDefects;
    }
    public String getLineId() {
        return lineId;
    }

}
