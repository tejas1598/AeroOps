package com.aeroops.springdemo.aircraft.exception;

public class AircraftScheduleConflictException extends RuntimeException {
    public AircraftScheduleConflictException(String message) {
        super(message);
    }
}
