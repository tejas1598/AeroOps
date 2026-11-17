package com.aeroops.springdemo.maintenance.service;

import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowRequestDTO;
import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import com.aeroops.springdemo.aircraft.entity.AircraftStatus;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import com.aeroops.springdemo.maintenance.entity.MaintenanceStatus;
import com.aeroops.springdemo.maintenance.entity.MaintenanceWindow;
import com.aeroops.springdemo.aircraft.exception.AircraftNotFoundException;
import com.aeroops.springdemo.aircraft.exception.AircraftUnavailableException;
import com.aeroops.springdemo.maintenance.exception.InvalidMaintenanceWindowException;
import com.aeroops.springdemo.maintenance.exception.MaintenanceWindowConflictException;
import com.aeroops.springdemo.maintenance.exception.MaintenanceWindowNotFoundException;
import com.aeroops.springdemo.maintenance.mapper.MaintenanceWindowMapper;
import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.flight.repository.FlightRepository;
import com.aeroops.springdemo.maintenance.repository.MaintenanceWindowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
public class MaintenanceWindowService {
    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final MaintenanceWindowMapper maintenanceWindowMapper;

    public MaintenanceWindowService(MaintenanceWindowRepository maintenanceWindowRepository,
                                    AircraftRepository aircraftRepository,
                                    FlightRepository flightRepository,
                                    MaintenanceWindowMapper maintenanceWindowMapper) {
        this.maintenanceWindowRepository = maintenanceWindowRepository;
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.maintenanceWindowMapper = maintenanceWindowMapper;
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceWindowResponseDTO> getWindows(Long aircraftId, Pageable pageable) {
        Page<MaintenanceWindow> windows = aircraftId == null
                ? maintenanceWindowRepository.findAll(pageable)
                : maintenanceWindowRepository.findAllByAircraftAircraftId(aircraftId, pageable);
        return windows.map(maintenanceWindowMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MaintenanceWindowResponseDTO getWindowById(Long id) {
        return maintenanceWindowMapper.toResponse(findWindow(id));
    }

    @Transactional
    public MaintenanceWindowResponseDTO scheduleWindow(MaintenanceWindowRequestDTO dto) {
        validateWindow(dto.getStartsAt(), dto.getEndsAt());
        Aircraft aircraft = aircraftRepository.findByIdForUpdate(dto.getAircraftId())
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with ID " + dto.getAircraftId() + " not found"));
        if (aircraft.getStatus() == AircraftStatus.RETIRED) {
            throw new AircraftUnavailableException("Retired aircraft cannot be scheduled for maintenance");
        }

        if (flightRepository.existsOverlappingAssignmentForAircraft(
                aircraft.getAircraftId(), FlightStatus.CANCELLED, dto.getStartsAt(), dto.getEndsAt())) {
            throw new MaintenanceWindowConflictException("Maintenance window overlaps an assigned flight");
        }
        if (maintenanceWindowRepository.existsOverlappingWindow(
                aircraft.getAircraftId(), MaintenanceStatus.SCHEDULED, dto.getStartsAt(), dto.getEndsAt())) {
            throw new MaintenanceWindowConflictException("Aircraft already has an overlapping maintenance window");
        }

        MaintenanceWindow window = new MaintenanceWindow(
                aircraft, dto.getStartsAt(), dto.getEndsAt(), dto.getReason().trim());
        return maintenanceWindowMapper.toResponse(maintenanceWindowRepository.save(window));
    }

    @Transactional
    public MaintenanceWindowResponseDTO cancelWindow(Long id) {
        MaintenanceWindow window = maintenanceWindowRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new MaintenanceWindowNotFoundException("Maintenance window with ID " + id + " not found"));
        if (window.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new MaintenanceWindowConflictException("Maintenance window is already cancelled");
        }
        if (!window.getStartsAt().isAfter(java.time.OffsetDateTime.now())) {
            throw new MaintenanceWindowConflictException("A maintenance window cannot be cancelled after it has started");
        }
        window.setStatus(MaintenanceStatus.CANCELLED);
        return maintenanceWindowMapper.toResponse(maintenanceWindowRepository.save(window));
    }

    private MaintenanceWindow findWindow(Long id) {
        return maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new MaintenanceWindowNotFoundException("Maintenance window with ID " + id + " not found"));
    }

    private void validateWindow(java.time.OffsetDateTime startsAt, java.time.OffsetDateTime endsAt) {
        if (startsAt != null && endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new InvalidMaintenanceWindowException("Maintenance end time must be after its start time");
        }
    }
}
