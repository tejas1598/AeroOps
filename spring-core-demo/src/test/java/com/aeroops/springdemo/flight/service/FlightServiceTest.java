package com.aeroops.springdemo.flight.service;

import com.aeroops.springdemo.flight.dto.FlightRequestDTO;
import com.aeroops.springdemo.flight.dto.FlightResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import com.aeroops.springdemo.aircraft.entity.AircraftStatus;
import com.aeroops.springdemo.airport.entity.Airport;
import com.aeroops.springdemo.flight.entity.Flight;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import com.aeroops.springdemo.aircraft.exception.AircraftScheduleConflictException;
import com.aeroops.springdemo.aircraft.exception.AircraftMaintenanceConflictException;
import com.aeroops.springdemo.aircraft.exception.AircraftUnavailableException;
import com.aeroops.springdemo.flight.exception.InvalidFlightScheduleException;
import com.aeroops.springdemo.flight.exception.InvalidFlightStatusTransitionException;
import com.aeroops.springdemo.flight.mapper.FlightMapper;
import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.airport.repository.AirportRepository;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {
    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private MaintenanceWindowRepository maintenanceWindowRepository;

    private FlightService flightService;

    @BeforeEach
    void setUp() {
        flightService = new FlightService(flightRepository, aircraftRepository, airportRepository,
                maintenanceWindowRepository, new FlightMapper());
    }

    @Test
    void scheduleFlightStoresNormalizedNumberAndAirportCodes() {
        Airport origin = new Airport("BOM", "Mumbai Airport", "Mumbai", "IN");
        Airport destination = new Airport("DEL", "Delhi Airport", "Delhi", "IN");
        FlightRequestDTO request = validRequest(1L, 2L);
        when(flightRepository.existsByFlightNumberIgnoreCaseAndScheduledDeparture("AI101", request.getScheduledDeparture()))
                .thenReturn(false);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(origin));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightResponseDTO scheduled = flightService.scheduleFlight(request);

        assertEquals("AI101", scheduled.getFlightNumber());
        assertEquals("BOM", scheduled.getOriginAirportCode());
        assertEquals("DEL", scheduled.getDestinationAirportCode());
    }

    @Test
    void scheduleFlightRejectsSameOriginAndDestination() {
        FlightRequestDTO request = validRequest(1L, 1L);

        assertThrows(InvalidFlightScheduleException.class, () -> flightService.scheduleFlight(request));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void scheduleFlightRejectsArrivalBeforeDeparture() {
        FlightRequestDTO request = validRequest(1L, 2L);
        request.setScheduledArrival(request.getScheduledDeparture().minusMinutes(1));

        assertThrows(InvalidFlightScheduleException.class, () -> flightService.scheduleFlight(request));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void assignAircraftAttachesReadyAircraftWhenScheduleIsClear() {
        Flight flight = flightForAssignment();
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.existsOverlappingAssignment(eq(20L), eq(10L), eq(FlightStatus.CANCELLED),
                eq(flight.getScheduledDeparture()), eq(flight.getScheduledArrival()))).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        flightService.assignAircraft(10L, 20L);

        assertSame(aircraft, flight.getAircraft());
        verify(flightRepository).save(flight);
    }

    @Test
    void assignAircraftRejectsAircraftUnderMaintenance() {
        Flight flight = flightForAssignment();
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        aircraft.setStatus(AircraftStatus.IN_MAINTENANCE);
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(aircraft));

        assertThrows(AircraftUnavailableException.class, () -> flightService.assignAircraft(10L, 20L));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void assignAircraftRejectsOverlappingFlight() {
        Flight flight = flightForAssignment();
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.existsOverlappingAssignment(eq(20L), eq(10L), eq(FlightStatus.CANCELLED),
                eq(flight.getScheduledDeparture()), eq(flight.getScheduledArrival()))).thenReturn(true);

        assertThrows(AircraftScheduleConflictException.class, () -> flightService.assignAircraft(10L, 20L));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void assignAircraftRejectsScheduledMaintenanceWindow() {
        Flight flight = flightForAssignment();
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(aircraft));
        when(maintenanceWindowRepository.existsOverlappingWindow(20L, com.aeroops.springdemo.maintenance.entity.MaintenanceStatus.SCHEDULED,
                flight.getScheduledDeparture(), flight.getScheduledArrival())).thenReturn(true);

        assertThrows(AircraftMaintenanceConflictException.class, () -> flightService.assignAircraft(10L, 20L));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void flightMustHaveAircraftBeforeBoarding() {
        Flight flight = flightForAssignment();
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));

        assertThrows(InvalidFlightStatusTransitionException.class,
                () -> flightService.updateStatus(10L, FlightStatus.BOARDING));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void assignedFlightCanMoveThroughItsValidLifecycle() {
        Flight flight = flightForAssignment();
        Aircraft aircraft = new Aircraft("VT-ABC", "Airbus", "A320", 180);
        ReflectionTestUtils.setField(aircraft, "aircraftId", 20L);
        flight.setAircraft(aircraft);
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightResponseDTO boarding = flightService.updateStatus(10L, FlightStatus.BOARDING);
        FlightResponseDTO inAir = flightService.updateStatus(10L, FlightStatus.IN_AIR);
        FlightResponseDTO landed = flightService.updateStatus(10L, FlightStatus.LANDED);

        assertEquals(FlightStatus.BOARDING, boarding.getStatus());
        assertEquals(FlightStatus.IN_AIR, inAir.getStatus());
        assertEquals(FlightStatus.LANDED, landed.getStatus());
    }

    @Test
    void landedFlightCannotBeReopened() {
        Flight flight = flightForAssignment();
        flight.setStatus(FlightStatus.LANDED);
        when(flightRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));

        assertThrows(InvalidFlightStatusTransitionException.class,
                () -> flightService.updateStatus(10L, FlightStatus.SCHEDULED));

        verify(flightRepository, never()).save(any(Flight.class));
    }

    private Flight flightForAssignment() {
        Airport origin = new Airport("BOM", "Mumbai Airport", "Mumbai", "IN");
        Airport destination = new Airport("DEL", "Delhi Airport", "Delhi", "IN");
        OffsetDateTime departure = OffsetDateTime.now().plusDays(2);
        return new Flight("AI101", origin, destination, departure, departure.plusHours(2));
    }

    private FlightRequestDTO validRequest(Long originAirportId, Long destinationAirportId) {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(2);
        FlightRequestDTO dto = new FlightRequestDTO();
        dto.setFlightNumber(" ai101 ");
        dto.setOriginAirportId(originAirportId);
        dto.setDestinationAirportId(destinationAirportId);
        dto.setScheduledDeparture(departure);
        dto.setScheduledArrival(departure.plusHours(2));
        return dto;
    }
}
