package com.aeroops.springdemo.flight.dto;

import com.aeroops.springdemo.flight.entity.FlightStatus;
import jakarta.validation.constraints.NotNull;

public class FlightStatusRequestDTO {
    @NotNull
    private FlightStatus status;

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }
}
