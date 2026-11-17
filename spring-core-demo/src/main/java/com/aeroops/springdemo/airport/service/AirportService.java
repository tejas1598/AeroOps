package com.aeroops.springdemo.airport.service;

import com.aeroops.springdemo.airport.dto.AirportRequestDTO;
import com.aeroops.springdemo.airport.dto.AirportResponseDTO;
import com.aeroops.springdemo.airport.entity.Airport;
import com.aeroops.springdemo.airport.exception.AirportAlreadyExistsException;
import com.aeroops.springdemo.airport.exception.AirportNotFoundException;
import com.aeroops.springdemo.airport.mapper.AirportMapper;
import com.aeroops.springdemo.airport.repository.AirportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AirportService {
    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;

    public AirportService(AirportRepository airportRepository, AirportMapper airportMapper) {
        this.airportRepository = airportRepository;
        this.airportMapper = airportMapper;
    }

    @Transactional(readOnly = true)
    public Page<AirportResponseDTO> getAirports(Pageable pageable) {
        return airportRepository.findAll(pageable).map(airportMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AirportResponseDTO getAirportById(Long id) {
        return airportMapper.toResponse(findAirport(id));
    }

    @Transactional
    public AirportResponseDTO createAirport(AirportRequestDTO dto) {
        String iataCode = normalizeCode(dto.getIataCode());
        ensureCodeAvailable(iataCode, null);
        Airport airport = airportMapper.toEntity(dto);
        airport.setIataCode(iataCode);
        return airportMapper.toResponse(airportRepository.save(airport));
    }

    @Transactional
    public AirportResponseDTO updateAirport(Long id, AirportRequestDTO dto) {
        Airport airport = findAirport(id);
        String iataCode = normalizeCode(dto.getIataCode());
        ensureCodeAvailable(iataCode, id);

        airport.setIataCode(iataCode);
        airport.setName(dto.getName().trim());
        airport.setCity(dto.getCity().trim());
        airport.setCountryCode(dto.getCountryCode().trim().toUpperCase());
        return airportMapper.toResponse(airportRepository.save(airport));
    }

    private Airport findAirport(Long id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new AirportNotFoundException("Airport with ID " + id + " not found"));
    }

    private void ensureCodeAvailable(String iataCode, Long airportId) {
        boolean alreadyUsed = airportId == null
                ? airportRepository.existsByIataCodeIgnoreCase(iataCode)
                : airportRepository.existsByIataCodeIgnoreCaseAndAirportIdNot(iataCode, airportId);
        if (alreadyUsed) {
            throw new AirportAlreadyExistsException("Airport code " + iataCode + " is already in use");
        }
    }

    private String normalizeCode(String iataCode) {
        return iataCode.trim().toUpperCase();
    }
}
