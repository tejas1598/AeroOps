package com.aeroops.springdemo.flight.controller;

import com.aeroops.springdemo.aircraft.dto.AircraftAssignmentRequestDTO;
import com.aeroops.springdemo.flight.dto.FlightRequestDTO;
import com.aeroops.springdemo.flight.dto.FlightResponseDTO;
import com.aeroops.springdemo.flight.dto.FlightStatusRequestDTO;
import com.aeroops.springdemo.flight.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<Page<FlightResponseDTO>> getFlights(
            @PageableDefault(size = 10, sort = "scheduledDeparture") Pageable pageable) {
        return ResponseEntity.ok(flightService.getFlights(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponseDTO> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @PostMapping
    public ResponseEntity<FlightResponseDTO> scheduleFlight(@Valid @RequestBody FlightRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.scheduleFlight(dto));
    }

    @PutMapping("/{id}/aircraft")
    public ResponseEntity<FlightResponseDTO> assignAircraft(
            @PathVariable Long id, @Valid @RequestBody AircraftAssignmentRequestDTO dto) {
        return ResponseEntity.ok(flightService.assignAircraft(id, dto.getAircraftId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FlightResponseDTO> updateStatus(
            @PathVariable Long id, @Valid @RequestBody FlightStatusRequestDTO dto) {
        return ResponseEntity.ok(flightService.updateStatus(id, dto.getStatus()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightResponseDTO> updateFlight(
            @PathVariable Long id, @Valid @RequestBody FlightRequestDTO dto) {
        return ResponseEntity.ok(flightService.updateFlight(id, dto));
    }
}
