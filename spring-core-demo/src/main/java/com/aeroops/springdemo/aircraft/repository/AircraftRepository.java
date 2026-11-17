package com.aeroops.springdemo.aircraft.repository;

import com.aeroops.springdemo.aircraft.entity.Aircraft;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    boolean existsByRegistrationCodeIgnoreCase(String registrationCode);
    boolean existsByRegistrationCodeIgnoreCaseAndAircraftIdNot(String registrationCode, Long aircraftId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select aircraft from Aircraft aircraft where aircraft.aircraftId = :aircraftId")
    Optional<Aircraft> findByIdForUpdate(@Param("aircraftId") Long aircraftId);
}
