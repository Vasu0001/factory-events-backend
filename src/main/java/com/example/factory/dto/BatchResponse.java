package com.example.factory.dto;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.ArrayList;
import java.util.List;

public class BatchResponse {

    public int accepted;
    public int updated;
    public int deduped;

    public int rejected;
    public int ignored;

    public List<Rejection> rejections = new ArrayList<>();

    public static class Rejection {
        public String eventId;
        public String reason;

        public Rejection(String eventId, String reason) {
            this.eventId = eventId;
            this.reason = reason;
        }

    }
    public int getAccepted() {
        return accepted;
    }

    public int getUpdated() {
        return updated;
    }

    public int getDeduped() {
        return deduped;
    }

    public int getRejected() {
        return rejected;
    }

    public int getIgnored() {
        return ignored;
    }
}
