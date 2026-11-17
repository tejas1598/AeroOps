package com.aeroops.springdemo.aircraft.service;

import com.aeroops.springdemo.aircraft.dto.AircraftRequestDTO;
import com.aeroops.springdemo.aircraft.dto.AircraftResponseDTO;
import com.aeroops.springdemo.aircraft.entity.Aircraft;
import com.aeroops.springdemo.aircraft.entity.AircraftStatus;
import com.aeroops.springdemo.flight.entity.FlightStatus;
import com.aeroops.springdemo.aircraft.exception.AircraftAlreadyExistsException;
import com.aeroops.springdemo.aircraft.exception.AircraftNotFoundException;
import com.aeroops.springdemo.aircraft.exception.AircraftUnavailableException;
import com.aeroops.springdemo.aircraft.mapper.AircraftMapper;
import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.flight.repository.FlightRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
public class AircraftService {
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final AircraftMapper aircraftMapper;

    public AircraftService(AircraftRepository aircraftRepository, FlightRepository flightRepository, AircraftMapper aircraftMapper) {
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.aircraftMapper = aircraftMapper;
    }

    @Transactional(readOnly = true)
    public Page<AircraftResponseDTO> getAircraft(Pageable pageable) {
        return aircraftRepository.findAll(pageable).map(aircraftMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AircraftResponseDTO getAircraftById(Long id) {
        return aircraftMapper.toResponse(findAircraft(id));
    }

    @Transactional
    public AircraftResponseDTO createAircraft(AircraftRequestDTO dto) {
        String registrationCode = normalizeRegistration(dto.getRegistrationCode());
        ensureRegistrationAvailable(registrationCode, null);
        Aircraft aircraft = aircraftMapper.toEntity(dto);
        aircraft.setRegistrationCode(registrationCode);
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Transactional
    public AircraftResponseDTO updateAircraft(Long id, AircraftRequestDTO dto) {
        Aircraft aircraft = findAircraft(id);
        String registrationCode = normalizeRegistration(dto.getRegistrationCode());
        ensureRegistrationAvailable(registrationCode, id);

        aircraft.setRegistrationCode(registrationCode);
        aircraft.setManufacturer(dto.getManufacturer().trim());
        aircraft.setModel(dto.getModel().trim());
        aircraft.setSeatingCapacity(dto.getSeatingCapacity());
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Transactional
    public AircraftResponseDTO updateStatus(Long id, AircraftStatus status) {
        Aircraft aircraft = aircraftRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with ID " + id + " not found"));
        if (status != AircraftStatus.READY && flightRepository.existsActiveAssignment(id,
                EnumSet.of(FlightStatus.SCHEDULED, FlightStatus.BOARDING, FlightStatus.IN_AIR))) {
            throw new AircraftUnavailableException("Aircraft has an active flight and cannot be taken out of service");
        }
        aircraft.setStatus(status);
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    private Aircraft findAircraft(Long id) {
        return aircraftRepository.findById(id)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with ID " + id + " not found"));
    }

    private void ensureRegistrationAvailable(String registrationCode, Long aircraftId) {
        boolean alreadyUsed = aircraftId == null
                ? aircraftRepository.existsByRegistrationCodeIgnoreCase(registrationCode)
                : aircraftRepository.existsByRegistrationCodeIgnoreCaseAndAircraftIdNot(registrationCode, aircraftId);
        if (alreadyUsed) {
            throw new AircraftAlreadyExistsException("Aircraft registration " + registrationCode + " is already in use");
        }
    }

    private String normalizeRegistration(String registrationCode) {
        return registrationCode.trim().toUpperCase();
    }
}
