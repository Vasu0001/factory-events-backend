package com.example.factory.controller;
import com.example.factory.dto.MachineStatsResponse;
import com.example.factory.dto.BatchResponse;
import com.example.factory.dto.EventRequest;
import com.example.factory.service.EventService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResponse> ingestBatch(
            @RequestBody List<EventRequest> events
    ) {
        BatchResponse response = service.processBatch(events);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/stats")
    public MachineStatsResponse getMachineStats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return service.getStats(machineId, start, end);
    }
}
