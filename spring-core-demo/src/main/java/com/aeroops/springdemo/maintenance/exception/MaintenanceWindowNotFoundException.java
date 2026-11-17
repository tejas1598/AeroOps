package com.aeroops.springdemo.maintenance.exception;

public class MaintenanceWindowNotFoundException extends RuntimeException {
    public MaintenanceWindowNotFoundException(String message) {
        super(message);
    }
}
