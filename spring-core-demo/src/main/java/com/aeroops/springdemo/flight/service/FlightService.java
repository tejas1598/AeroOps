package com.aeroops.springdemo.flight.service;

import com.aeroops.springdemo.flight.dto.FlightRequestDTO;
import com.aeroops.springdemo.flight.dto.FlightResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import com.aeroops.springdemo.aircraft.entity.AircraftStatus;
import com.aeroops.springdemo.airport.entity.Airport;
import com.aeroops.springdemo.flight.entity.Flight;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import com.aeroops.springdemo.maintenance.entity.MaintenanceStatus;
import com.aeroops.springdemo.aircraft.exception.AircraftNotFoundException;
import com.aeroops.springdemo.aircraft.exception.AircraftScheduleConflictException;
import com.aeroops.springdemo.aircraft.exception.AircraftMaintenanceConflictException;
import com.aeroops.springdemo.aircraft.exception.AircraftUnavailableException;
import com.aeroops.springdemo.airport.exception.AirportNotFoundException;
import com.aeroops.springdemo.flight.exception.FlightAlreadyExistsException;
import com.aeroops.springdemo.flight.exception.FlightNotFoundException;
import com.aeroops.springdemo.flight.exception.FlightNotAssignableException;
import com.aeroops.springdemo.flight.exception.InvalidFlightScheduleException;
import com.aeroops.springdemo.flight.exception.InvalidFlightStatusTransitionException;
import com.aeroops.springdemo.flight.mapper.FlightMapper;
import com.aeroops.springdemo.airport.repository.AirportRepository;
import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.flight.repository.FlightRepository;
import com.aeroops.springdemo.maintenance.repository.MaintenanceWindowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final FlightMapper flightMapper;

    public FlightService(FlightRepository flightRepository, AircraftRepository aircraftRepository,
                         AirportRepository airportRepository, MaintenanceWindowRepository maintenanceWindowRepository,
                         FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.maintenanceWindowRepository = maintenanceWindowRepository;
        this.flightMapper = flightMapper;
    }

    @Transactional(readOnly = true)
    public Page<FlightResponseDTO> getFlights(Pageable pageable) {
        return flightRepository.findAll(pageable).map(flightMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FlightResponseDTO getFlightById(Long id) {
        return flightMapper.toResponse(findFlight(id));
    }

    @Transactional
    public FlightResponseDTO scheduleFlight(FlightRequestDTO dto) {
        validateSchedule(dto);
        String flightNumber = normalizeFlightNumber(dto.getFlightNumber());
        ensureFlightNumberAvailable(flightNumber, dto.getScheduledDeparture(), null);

        Airport origin = findAirport(dto.getOriginAirportId());
        Airport destination = findAirport(dto.getDestinationAirportId());
        Flight flight = new Flight(flightNumber, origin, destination,
                dto.getScheduledDeparture(), dto.getScheduledArrival());
        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Transactional
    public FlightResponseDTO updateFlight(Long id, FlightRequestDTO dto) {
        validateSchedule(dto);
        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + id + " not found"));
        String flightNumber = normalizeFlightNumber(dto.getFlightNumber());
        ensureFlightNumberAvailable(flightNumber, dto.getScheduledDeparture(), id);

        if (flight.getAircraft() != null) {
            Long assignedAircraftId = flight.getAircraft().getAircraftId();
            Aircraft aircraft = aircraftRepository.findByIdForUpdate(assignedAircraftId)
                    .orElseThrow(() -> new AircraftNotFoundException("Aircraft with ID " + assignedAircraftId + " not found"));
            if (aircraft.getStatus() != AircraftStatus.READY) {
                throw new AircraftUnavailableException("Assigned aircraft is no longer ready for flight scheduling");
            }
            if (flightRepository.existsOverlappingAssignment(
                    assignedAircraftId, id, FlightStatus.CANCELLED,
                    dto.getScheduledDeparture(), dto.getScheduledArrival())) {
                throw new AircraftScheduleConflictException("The updated schedule conflicts with another flight for this aircraft");
            }
            if (maintenanceWindowRepository.existsOverlappingWindow(assignedAircraftId, MaintenanceStatus.SCHEDULED,
                    dto.getScheduledDeparture(), dto.getScheduledArrival())) {
                throw new AircraftMaintenanceConflictException("The updated schedule overlaps aircraft maintenance");
            }
        }

        flight.setFlightNumber(flightNumber);
        flight.setOriginAirport(findAirport(dto.getOriginAirportId()));
        flight.setDestinationAirport(findAirport(dto.getDestinationAirportId()));
        flight.setScheduledDeparture(dto.getScheduledDeparture());
        flight.setScheduledArrival(dto.getScheduledArrival());
        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Transactional
    public FlightResponseDTO assignAircraft(Long flightId, Long aircraftId) {
        Flight flight = flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + flightId + " not found"));
        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new FlightNotAssignableException("Aircraft can only be assigned to a scheduled flight");
        }

        Aircraft aircraft = aircraftRepository.findByIdForUpdate(aircraftId)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with ID " + aircraftId + " not found"));
        if (aircraft.getStatus() != AircraftStatus.READY) {
            throw new AircraftUnavailableException("Aircraft " + aircraft.getRegistrationCode() + " is not ready for assignment");
        }

        boolean scheduleConflict = flightRepository.existsOverlappingAssignment(
                aircraftId, flightId, FlightStatus.CANCELLED,
                flight.getScheduledDeparture(), flight.getScheduledArrival());
        if (scheduleConflict) {
            throw new AircraftScheduleConflictException("Aircraft already has a flight during this time window");
        }
        if (maintenanceWindowRepository.existsOverlappingWindow(aircraftId, MaintenanceStatus.SCHEDULED,
                flight.getScheduledDeparture(), flight.getScheduledArrival())) {
            throw new AircraftMaintenanceConflictException("Aircraft is scheduled for maintenance during this flight");
        }

        flight.setAircraft(aircraft);
        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Transactional
    public FlightResponseDTO updateStatus(Long flightId, FlightStatus targetStatus) {
        Flight flight = flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + flightId + " not found"));
        FlightStatus currentStatus = flight.getStatus();
        if (!canTransition(currentStatus, targetStatus)) {
            throw new InvalidFlightStatusTransitionException(
                    "Flight cannot transition from " + currentStatus + " to " + targetStatus);
        }

        if (targetStatus == FlightStatus.BOARDING || targetStatus == FlightStatus.IN_AIR) {
            if (flight.getAircraft() == null) {
                throw new InvalidFlightStatusTransitionException(
                        "An aircraft must be assigned before a flight can board or depart");
            }
            Long aircraftId = flight.getAircraft().getAircraftId();
            Aircraft aircraft = aircraftRepository.findByIdForUpdate(aircraftId)
                    .orElseThrow(() -> new AircraftNotFoundException("Aircraft with ID " + aircraftId + " not found"));
            if (aircraft.getStatus() != AircraftStatus.READY) {
                throw new AircraftUnavailableException("The assigned aircraft is not ready for operations");
            }
        }

        flight.setStatus(targetStatus);
        return flightMapper.toResponse(flightRepository.save(flight));
    }

    private boolean canTransition(FlightStatus currentStatus, FlightStatus targetStatus) {
        return switch (currentStatus) {
            case SCHEDULED -> targetStatus == FlightStatus.BOARDING || targetStatus == FlightStatus.CANCELLED;
            case BOARDING -> targetStatus == FlightStatus.IN_AIR || targetStatus == FlightStatus.CANCELLED;
            case IN_AIR -> targetStatus == FlightStatus.LANDED;
            case LANDED, CANCELLED -> false;
        };
    }

    private void validateSchedule(FlightRequestDTO dto) {
        if (dto.getOriginAirportId() != null && dto.getOriginAirportId().equals(dto.getDestinationAirportId())) {
            throw new InvalidFlightScheduleException("Origin and destination airports must be different");
        }
        if (dto.getScheduledDeparture() != null && dto.getScheduledArrival() != null
                && !dto.getScheduledArrival().isAfter(dto.getScheduledDeparture())) {
            throw new InvalidFlightScheduleException("Scheduled arrival must be after scheduled departure");
        }
    }

    private Airport findAirport(Long id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new AirportNotFoundException("Airport with ID " + id + " not found"));
    }

    private Flight findFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + id + " not found"));
    }

    private void ensureFlightNumberAvailable(String flightNumber, java.time.OffsetDateTime departure, Long flightId) {
        boolean alreadyUsed = flightId == null
                ? flightRepository.existsByFlightNumberIgnoreCaseAndScheduledDeparture(flightNumber, departure)
                : flightRepository.existsByFlightNumberIgnoreCaseAndScheduledDepartureAndFlightIdNot(flightNumber, departure, flightId);
        if (alreadyUsed) {
            throw new FlightAlreadyExistsException(
                    "Flight " + flightNumber + " is already scheduled for that departure time");
        }
    }

    private String normalizeFlightNumber(String flightNumber) {
        return flightNumber.trim().toUpperCase();
    }
}
