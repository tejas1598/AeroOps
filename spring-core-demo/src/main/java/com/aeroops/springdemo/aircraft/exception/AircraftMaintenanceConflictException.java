package com.aeroops.springdemo.aircraft.exception;

public class AircraftMaintenanceConflictException extends RuntimeException {
    public AircraftMaintenanceConflictException(String message) {
        super(message);
    }
}
