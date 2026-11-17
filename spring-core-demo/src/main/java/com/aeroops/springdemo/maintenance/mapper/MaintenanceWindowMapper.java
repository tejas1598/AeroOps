package com.aeroops.springdemo.maintenance.mapper;

import com.aeroops.springdemo.maintenance.dto.MaintenanceWindowResponseDTO;
import com.aeroops.springdemo.maintenance.entity.MaintenanceWindow;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceWindowMapper {
    public MaintenanceWindowResponseDTO toResponse(MaintenanceWindow window) {
        MaintenanceWindowResponseDTO dto = new MaintenanceWindowResponseDTO();
        dto.setMaintenanceWindowId(window.getMaintenanceWindowId());
        dto.setAircraftId(window.getAircraft().getAircraftId());
        dto.setAircraftRegistrationCode(window.getAircraft().getRegistrationCode());
        dto.setStartsAt(window.getStartsAt());
        dto.setEndsAt(window.getEndsAt());
        dto.setReason(window.getReason());
        dto.setStatus(window.getStatus());
        return dto;
    }
}
