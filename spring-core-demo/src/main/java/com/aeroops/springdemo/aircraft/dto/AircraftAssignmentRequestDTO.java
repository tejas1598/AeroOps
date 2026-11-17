package com.aeroops.springdemo.aircraft.dto;

import jakarta.validation.constraints.NotNull;

public class AircraftAssignmentRequestDTO {
    @NotNull
    private Long aircraftId;

    public Long getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(Long aircraftId) {
        this.aircraftId = aircraftId;
    }
}
