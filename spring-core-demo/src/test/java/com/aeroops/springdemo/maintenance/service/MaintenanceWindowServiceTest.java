package com.aeroops.springdemo.maintenance.service;

import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowRequestDTO;
import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import com.aeroops.springdemo.maintenance.entity.MaintenanceStatus;
import com.aeroops.springdemo.maintenance.entity.MaintenanceWindow;
import com.aeroops.springdemo.maintenance.exception.InvalidMaintenanceWindowException;
import com.aeroops.springdemo.maintenance.exception.MaintenanceWindowConflictException;
import com.aeroops.springdemo.maintenance.mapper.MaintenanceWindowMapper;
import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.flight.repository.FlightRepository;
import com.aeroops.springdemo.maintenance.repository.MaintenanceWindowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceWindowServiceTest {
    @Mock
    private MaintenanceWindowRepository maintenanceWindowRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private FlightRepository flightRepository;

    private MaintenanceWindowService maintenanceWindowService;

    @BeforeEach
    void setUp() {
        maintenanceWindowService = new MaintenanceWindowService(
                maintenanceWindowRepository, aircraftRepository, flightRepository, new MaintenanceWindowMapper());
    }

    @Test
    void schedulesWindowWhenFlightAndMaintenanceCalendarAreClear() {
        Aircraft aircraft = aircraft();
        MaintenanceWindowRequestDTO request = validRequest();
        when(aircraftRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.existsOverlappingAssignmentForAircraft(42L, FlightStatus.CANCELLED,
                request.getStartsAt(), request.getEndsAt())).thenReturn(false);
        when(maintenanceWindowRepository.existsOverlappingWindow(42L, MaintenanceStatus.SCHEDULED,
                request.getStartsAt(), request.getEndsAt())).thenReturn(false);
        when(maintenanceWindowRepository.save(any(MaintenanceWindow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceWindowResponseDTO scheduled = maintenanceWindowService.scheduleWindow(request);

        assertEquals("VT-ABC", scheduled.getAircraftRegistrationCode());
        assertEquals(MaintenanceStatus.SCHEDULED, scheduled.getStatus());
        verify(maintenanceWindowRepository).save(any(MaintenanceWindow.class));
    }

    @Test
    void rejectsMaintenanceThatOverlapsAnAssignedFlight() {
        Aircraft aircraft = aircraft();
        MaintenanceWindowRequestDTO request = validRequest();
        when(aircraftRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.existsOverlappingAssignmentForAircraft(42L, FlightStatus.CANCELLED,
                request.getStartsAt(), request.getEndsAt())).thenReturn(true);

        assertThrows(MaintenanceWindowConflictException.class,
                () -> maintenanceWindowService.scheduleWindow(request));

        verify(maintenanceWindowRepository, never()).save(any(MaintenanceWindow.class));
    }

    @Test
    void rejectsOverlappingMaintenanceWindows() {
        Aircraft aircraft = aircraft();
        MaintenanceWindowRequestDTO request = validRequest();
        when(aircraftRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.existsOverlappingAssignmentForAircraft(42L, FlightStatus.CANCELLED,
                request.getStartsAt(), request.getEndsAt())).thenReturn(false);
        when(maintenanceWindowRepository.existsOverlappingWindow(42L, MaintenanceStatus.SCHEDULED,
                request.getStartsAt(), request.getEndsAt())).thenReturn(true);

        assertThrows(MaintenanceWindowConflictException.class,
                () -> maintenanceWindowService.scheduleWindow(request));

        verify(maintenanceWindowRepository, never()).save(any(MaintenanceWindow.class));
    }

    @Test
    void rejectsWindowWhenEndIsNotAfterStart() {
        MaintenanceWindowRequestDTO request = validRequest();
        request.setEndsAt(request.getStartsAt());

        assertThrows(InvalidMaintenanceWindowException.class,
                () -> maintenanceWindowService.scheduleWindow(request));

        verify(aircraftRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void cancellationIsRecordedForFutureWindow() {
        Aircraft aircraft = aircraft();
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        MaintenanceWindow window = new MaintenanceWindow(aircraft, start, start.plusHours(2), "Inspection");
        when(maintenanceWindowRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(window));
        when(maintenanceWindowRepository.save(any(MaintenanceWindow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceWindowResponseDTO cancelled = maintenanceWindowService.cancelWindow(7L);

        assertEquals(MaintenanceStatus.CANCELLED, cancelled.getStatus());
    }

    private Aircraft aircraft() {
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        ReflectionTestUtils.setField(aircraft, "aircraftId", 42L);
        return aircraft;
    }

    private MaintenanceWindowRequestDTO validRequest() {
        OffsetDateTime startsAt = OffsetDateTime.now().plusDays(10);
        MaintenanceWindowRequestDTO dto = new MaintenanceWindowRequestDTO();
        dto.setAircraftId(42L);
        dto.setStartsAt(startsAt);
        dto.setEndsAt(startsAt.plusHours(3));
        dto.setReason("Scheduled inspection");
        return dto;
    }
}
