package com.aeroops.springdemo.aircraft.controller;

import com.aeroops.springdemo.aircraft.dto.AircraftRequestDTO;
import com.aeroops.springdemo.aircraft.dto.AircraftResponseDTO;
import com.aeroops.springdemo.aircraft.dto.AircraftStatusRequestDTO;
import com.aeroops.springdemo.aircraft.service.AircraftService;
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
@RequestMapping("/api/v1/aircraft")
public class AircraftController {
    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @GetMapping
    public ResponseEntity<Page<AircraftResponseDTO>> getAircraft(
            @PageableDefault(size = 10, sort = "registrationCode") Pageable pageable) {
        return ResponseEntity.ok(aircraftService.getAircraft(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftResponseDTO> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraftById(id));
    }

    @PostMapping
    public ResponseEntity<AircraftResponseDTO> createAircraft(@Valid @RequestBody AircraftRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aircraftService.createAircraft(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftResponseDTO> updateAircraft(
            @PathVariable Long id, @Valid @RequestBody AircraftRequestDTO dto) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AircraftResponseDTO> updateStatus(
            @PathVariable Long id, @Valid @RequestBody AircraftStatusRequestDTO dto) {
        return ResponseEntity.ok(aircraftService.updateStatus(id, dto.getStatus()));
    }
}
