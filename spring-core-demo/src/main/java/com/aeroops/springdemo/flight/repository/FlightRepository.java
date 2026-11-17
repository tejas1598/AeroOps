package com.aeroops.springdemo.flight.repository;

import com.aeroops.springdemo.flight.entity.Flight;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    boolean existsByFlightNumberIgnoreCaseAndScheduledDeparture(String flightNumber, OffsetDateTime scheduledDeparture);
    boolean existsByFlightNumberIgnoreCaseAndScheduledDepartureAndFlightIdNot(String flightNumber, OffsetDateTime scheduledDeparture, Long flightId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select flight from Flight flight where flight.flightId = :flightId")
    Optional<Flight> findByIdForUpdate(@Param("flightId") Long flightId);

    @Query("""
            select case when count(flight) > 0 then true else false end
            from Flight flight
            where flight.aircraft.aircraftId = :aircraftId
              and flight.flightId <> :currentFlightId
              and flight.status <> :cancelledStatus
              and flight.scheduledDeparture < :candidateArrival
              and flight.scheduledArrival > :candidateDeparture
            """)
    boolean existsOverlappingAssignment(
            @Param("aircraftId") Long aircraftId,
            @Param("currentFlightId") Long currentFlightId,
            @Param("cancelledStatus") FlightStatus cancelledStatus,
            @Param("candidateDeparture") OffsetDateTime candidateDeparture,
            @Param("candidateArrival") OffsetDateTime candidateArrival);

    @Query("""
            select case when count(flight) > 0 then true else false end
            from Flight flight
            where flight.aircraft.aircraftId = :aircraftId
              and flight.status <> :cancelledStatus
              and flight.scheduledDeparture < :candidateArrival
              and flight.scheduledArrival > :candidateDeparture
            """)
    boolean existsOverlappingAssignmentForAircraft(
            @Param("aircraftId") Long aircraftId,
            @Param("cancelledStatus") FlightStatus cancelledStatus,
            @Param("candidateDeparture") OffsetDateTime candidateDeparture,
            @Param("candidateArrival") OffsetDateTime candidateArrival);

    @Query("""
            select case when count(flight) > 0 then true else false end
            from Flight flight
            where flight.aircraft.aircraftId = :aircraftId
              and flight.status in :activeStatuses
            """)
    boolean existsActiveAssignment(
            @Param("aircraftId") Long aircraftId,
            @Param("activeStatuses") Collection<FlightStatus> activeStatuses);
}
