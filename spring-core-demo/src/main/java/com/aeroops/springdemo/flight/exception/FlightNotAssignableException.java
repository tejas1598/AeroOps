package com.aeroops.springdemo.flight.exception;

public class FlightNotAssignableException extends RuntimeException {
    public FlightNotAssignableException(String message) {
        super(message);
    }
}
