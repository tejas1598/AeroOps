package com.aeroops.springdemo.maintenance.dto;

import com.aeroops.springdemo.maintenance.entity.MaintenanceStatus;

import java.time.OffsetDateTime;

public class MaintenanceWindowResponseDTO {
    private Long maintenanceWindowId;
    private Long aircraftId;
    private String aircraftRegistrationCode;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String reason;
    private MaintenanceStatus status;

    public Long getMaintenanceWindowId() {
        return maintenanceWindowId;
    }

    public void setMaintenanceWindowId(Long maintenanceWindowId) {
        this.maintenanceWindowId = maintenanceWindowId;
    }

    public Long getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(Long aircraftId) {
        this.aircraftId = aircraftId;
    }

    public String getAircraftRegistrationCode() {
        return aircraftRegistrationCode;
    }

    public void setAircraftRegistrationCode(String aircraftRegistrationCode) {
        this.aircraftRegistrationCode = aircraftRegistrationCode;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }
}
