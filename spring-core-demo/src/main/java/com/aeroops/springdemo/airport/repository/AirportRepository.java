package com.aeroops.springdemo.airport.repository;

import com.aeroops.springdemo.airport.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    boolean existsByIataCodeIgnoreCase(String iataCode);
    boolean existsByIataCodeIgnoreCaseAndAirportIdNot(String iataCode, Long airportId);
}
