package com.aeroops.springdemo.aircraft.mapper;

import com.aeroops.springdemo.aircraft.dto.AircraftRequestDTO;
import com.aeroops.springdemo.aircraft.dto.AircraftResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import org.springframework.stereotype.Component;

@Component
public class AircraftMapper {
    public Aircraft toEntity(AircraftRequestDTO dto) {
        return new Aircraft(
                dto.getRegistrationCode().trim().toUpperCase(),
                dto.getManufacturer().trim(),
                dto.getModel().trim(),
                dto.getSeatingCapacity()
        );
    }

    public AircraftResponseDTO toResponse(Aircraft aircraft) {
        AircraftResponseDTO dto = new AircraftResponseDTO();
        dto.setAircraftId(aircraft.getAircraftId());
        dto.setRegistrationCode(aircraft.getRegistrationCode());
        dto.setManufacturer(aircraft.getManufacturer());
        dto.setModel(aircraft.getModel());
        dto.setSeatingCapacity(aircraft.getSeatingCapacity());
        dto.setStatus(aircraft.getStatus());
        return dto;
    }
}
