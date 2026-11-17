package com.aeroops.springdemo.maintenance.exception;

public class InvalidMaintenanceWindowException extends RuntimeException {
    public InvalidMaintenanceWindowException(String message) {
        super(message);
    }
}
