package com.example.factory.controller;

import com.example.factory.dto.MachineStatsResponse;
import com.example.factory.dto.TopDefectLineResponse;
import com.example.factory.service.EventService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final EventService eventService;

    public StatsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public MachineStatsResponse getMachineStats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end
    ) {
        return eventService.getMachineStats(machineId, start, end);
    }

    @GetMapping("/top-defect-lines")
    public List<TopDefectLineResponse> getTopDefectLines(
            @RequestParam String factoryId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return eventService.getTopDefectLines(factoryId, from, to, limit);
    }
}
