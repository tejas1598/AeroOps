package com.aeroops.springdemo.airport.controller;

import com.aeroops.springdemo.airport.dto.AirportRequestDTO;
import com.aeroops.springdemo.airport.dto.AirportResponseDTO;
import com.aeroops.springdemo.airport.service.AirportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/airports")
public class AirportController {
    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping
    public ResponseEntity<Page<AirportResponseDTO>> getAirports(
            @PageableDefault(size = 10, sort = "iataCode") Pageable pageable) {
        return ResponseEntity.ok(airportService.getAirports(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponseDTO> getAirportById(@PathVariable Long id) {
        return ResponseEntity.ok(airportService.getAirportById(id));
    }

    @PostMapping
    public ResponseEntity<AirportResponseDTO> createAirport(@Valid @RequestBody AirportRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.createAirport(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirportResponseDTO> updateAirport(
            @PathVariable Long id, @Valid @RequestBody AirportRequestDTO dto) {
        return ResponseEntity.ok(airportService.updateAirport(id, dto));
    }
}
