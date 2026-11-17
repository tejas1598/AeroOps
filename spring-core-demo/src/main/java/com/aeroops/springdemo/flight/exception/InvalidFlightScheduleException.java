package com.aeroops.springdemo.flight.exception;

public class InvalidFlightScheduleException extends RuntimeException {
    public InvalidFlightScheduleException(String message) {
        super(message);
    }
}
