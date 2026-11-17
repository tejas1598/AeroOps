package com.aeroops.springdemo.maintenance.exception;

public class MaintenanceWindowConflictException extends RuntimeException {
    public MaintenanceWindowConflictException(String message) {
        super(message);
    }
}
