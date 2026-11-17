package com.aeroops.springdemo.airport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AirportRequestDTO {
    @NotBlank
    @Pattern(regexp = "[A-Za-z]{3}", message = "Airport code must contain exactly three letters")
    private String iataCode;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 80)
    private String city;

    @NotBlank
    @Pattern(regexp = "[A-Za-z]{2}", message = "Country code must contain exactly two letters")
    private String countryCode;

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
