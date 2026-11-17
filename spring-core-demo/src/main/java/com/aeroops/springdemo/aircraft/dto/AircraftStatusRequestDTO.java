package com.aeroops.springdemo.aircraft.dto;

import com.aeroops.springdemo.aircraft.entity.AircraftStatus;
import jakarta.validation.constraints.NotNull;

public class AircraftStatusRequestDTO {
    @NotNull
    private AircraftStatus status;

    public AircraftStatus getStatus() {
        return status;
    }

    public void setStatus(AircraftStatus status) {
        this.status = status;
    }
}
