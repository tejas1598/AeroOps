package com.aeroops.springdemo.maintenance.controller;

import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowRequestDTO;
import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowResponseDTO;
import com.aeroops.springdemo.maintenance.service.MaintenanceWindowService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maintenance-windows")
public class MaintenanceWindowController {
    private final MaintenanceWindowService maintenanceWindowService;

    public MaintenanceWindowController(MaintenanceWindowService maintenanceWindowService) {
        this.maintenanceWindowService = maintenanceWindowService;
    }

    @GetMapping
    public ResponseEntity<Page<MaintenanceWindowResponseDTO>> getWindows(
            @RequestParam(required = false) Long aircraftId,
            @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {
        return ResponseEntity.ok(maintenanceWindowService.getWindows(aircraftId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceWindowResponseDTO> getWindowById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceWindowService.getWindowById(id));
    }

    @PostMapping
    public ResponseEntity<MaintenanceWindowResponseDTO> scheduleWindow(
            @Valid @RequestBody MaintenanceWindowRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceWindowService.scheduleWindow(dto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MaintenanceWindowResponseDTO> cancelWindow(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceWindowService.cancelWindow(id));
    }
}
