package com.aeroops.springdemo.flight.mapper;

import com.aeroops.springdemo.flight.dto.FlightResponseDTO;
import com.aeroops.springdemo.flight.entity.Flight;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {
    public FlightResponseDTO toResponse(Flight flight) {
        FlightResponseDTO dto = new FlightResponseDTO();
        dto.setFlightId(flight.getFlightId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setOriginAirportCode(flight.getOriginAirport().getIataCode());
        dto.setDestinationAirportCode(flight.getDestinationAirport().getIataCode());
        dto.setScheduledDeparture(flight.getScheduledDeparture());
        dto.setScheduledArrival(flight.getScheduledArrival());
        dto.setStatus(flight.getStatus());
        dto.setAircraftId(flight.getAircraft() == null ? null : flight.getAircraft().getAircraftId());
        return dto;
    }
}
