package com.aeroops.springdemo.aircraft.service;

import com.aeroops.springdemo.aircraft.dto.AircraftRequestDTO;
import com.aeroops.springdemo.aircraft.dto.AircraftResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import com.aeroops.springdemo.aircraft.entity.AircraftStatus;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import com.aeroops.springdemo.aircraft.exception.AircraftAlreadyExistsException;
import com.aeroops.springdemo.aircraft.exception.AircraftUnavailableException;
import com.aeroops.springdemo.aircraft.mapper.AircraftMapper;
import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.flight.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {
    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private FlightRepository flightRepository;

    private AircraftService aircraftService;

    @BeforeEach
    void setUp() {
        aircraftService = new AircraftService(aircraftRepository, flightRepository, new AircraftMapper());
    }

    @Test
    void createAircraftNormalizesRegistrationAndStartsReady() {
        when(aircraftRepository.existsByRegistrationCodeIgnoreCase("VT-ABC")).thenReturn(false);
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AircraftResponseDTO created = aircraftService.createAircraft(validRequest(" vt-abc "));

        assertEquals("VT-ABC", created.getRegistrationCode());
        assertEquals(AircraftStatus.READY, created.getStatus());
        verify(aircraftRepository).save(any(Aircraft.class));
    }

    @Test
    void createAircraftRejectsDuplicateRegistration() {
        when(aircraftRepository.existsByRegistrationCodeIgnoreCase("VT-ABC")).thenReturn(true);

        assertThrows(AircraftAlreadyExistsException.class,
                () -> aircraftService.createAircraft(validRequest("vt-abc")));

        verify(aircraftRepository, never()).save(any(Aircraft.class));
    }

    @Test
    void updateStatusChangesReadinessWithoutChangingAircraftDetails() {
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        when(aircraftRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(aircraft));
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AircraftResponseDTO updated = aircraftService.updateStatus(1L, AircraftStatus.IN_MAINTENANCE);

        assertEquals(AircraftStatus.IN_MAINTENANCE, updated.getStatus());
        assertEquals("VT-ABC", updated.getRegistrationCode());
    }

    @Test
    void aircraftWithActiveFlightCannotBeTakenOutOfService() {
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        when(aircraftRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.existsActiveAssignment(1L,
                java.util.EnumSet.of(FlightStatus.SCHEDULED, FlightStatus.BOARDING, FlightStatus.IN_AIR)))
                .thenReturn(true);

        assertThrows(AircraftUnavailableException.class,
                () -> aircraftService.updateStatus(1L, AircraftStatus.IN_MAINTENANCE));

        verify(aircraftRepository, never()).save(any(Aircraft.class));
    }

    private AircraftRequestDTO validRequest(String registrationCode) {
        AircraftRequestDTO dto = new AircraftRequestDTO();
        dto.setRegistrationCode(registrationCode);
        dto.setManufacturer("Airbus");
        dto.setModel("A320");
        dto.setSeatingCapacity(180);
        return dto;
    }
}
