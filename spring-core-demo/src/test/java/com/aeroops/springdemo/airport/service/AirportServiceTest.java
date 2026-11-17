package com.aeroops.springdemo.airport.service;

import com.aeroops.springdemo.airport.dto.AirportRequestDTO;
import com.aeroops.springdemo.airport.dto.AirportResponseDTO;
import com.aeroops.springdemo.airport.entity.Airport;
import com.aeroops.springdemo.airport.exception.AirportAlreadyExistsException;
import com.aeroops.springdemo.airport.mapper.AirportMapper;
import com.aeroops.springdemo.airport.repository.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {
    @Mock
    private AirportRepository airportRepository;

    private AirportService airportService;

    @BeforeEach
    void setUp() {
        airportService = new AirportService(airportRepository, new AirportMapper());
    }

    @Test
    void createAirportNormalizesCodes() {
        when(airportRepository.existsByIataCodeIgnoreCase("BOM")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AirportResponseDTO created = airportService.createAirport(validRequest(" bom ", " in "));

        assertEquals("BOM", created.getIataCode());
        assertEquals("IN", created.getCountryCode());
    }

    @Test
    void createAirportRejectsDuplicateCode() {
        when(airportRepository.existsByIataCodeIgnoreCase("BOM")).thenReturn(true);

        assertThrows(AirportAlreadyExistsException.class,
                () -> airportService.createAirport(validRequest("bom", "IN")));

        verify(airportRepository, never()).save(any(Airport.class));
    }

    private AirportRequestDTO validRequest(String iataCode, String countryCode) {
        AirportRequestDTO dto = new AirportRequestDTO();
        dto.setIataCode(iataCode);
        dto.setName("Chhatrapati Shivaji Maharaj International Airport");
        dto.setCity("Mumbai");
        dto.setCountryCode(countryCode);
        return dto;
    }
}
