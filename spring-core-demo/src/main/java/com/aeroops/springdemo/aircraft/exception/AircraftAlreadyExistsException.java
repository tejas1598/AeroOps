package com.aeroops.springdemo.aircraft.exception;

public class AircraftAlreadyExistsException extends RuntimeException {
    public AircraftAlreadyExistsException(String message) {
        super(message);
    }
}
