package com.aeroops.springdemo.aircraft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aircraft")
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftId;

    @Column(nullable = false, unique = true, length = 16)
    private String registrationCode;

    @Column(nullable = false, length = 80)
    private String manufacturer;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false)
    private Integer seatingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AircraftStatus status = AircraftStatus.READY;

    protected Aircraft() {
    }

    public Aircraft(String registrationCode, String manufacturer, String model, Integer seatingCapacity) {
        this.registrationCode = registrationCode;
        this.manufacturer = manufacturer;
        this.model = model;
        this.seatingCapacity = seatingCapacity;
        this.status = AircraftStatus.READY;
    }

    public Long getAircraftId() {
        return aircraftId;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public AircraftStatus getStatus() {
        return status;
    }

    public void setStatus(AircraftStatus status) {
        this.status = status;
    }
}
