package com.aeroops.springdemo.airport.mapper;

import com.aeroops.springdemo.airport.dto.AirportRequestDTO;
import com.aeroops.springdemo.airport.dto.AirportResponseDTO;
import com.aeroops.springdemo.airport.entity.Airport;
import org.springframework.stereotype.Component;

@Component
public class AirportMapper {
    public Airport toEntity(AirportRequestDTO dto) {
        return new Airport(
                dto.getIataCode().trim().toUpperCase(),
                dto.getName().trim(),
                dto.getCity().trim(),
                dto.getCountryCode().trim().toUpperCase()
        );
    }

    public AirportResponseDTO toResponse(Airport airport) {
        AirportResponseDTO dto = new AirportResponseDTO();
        dto.setAirportId(airport.getAirportId());
        dto.setIataCode(airport.getIataCode());
        dto.setName(airport.getName());
        dto.setCity(airport.getCity());
        dto.setCountryCode(airport.getCountryCode());
        return dto;
    }
}
