package com.aeroops.springdemo.aircraft.exception;

public class AircraftUnavailableException extends RuntimeException {
    public AircraftUnavailableException(String message) {
        super(message);
    }
}
