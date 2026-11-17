package com.aeroops.springdemo.maintenance.repository;

import com.aeroops.springdemo.maintenance.entity.MaintenanceStatus;
import com.aeroops.springdemo.maintenance.entity.MaintenanceWindow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, Long> {
    Page<MaintenanceWindow> findAllByAircraftAircraftId(Long aircraftId, Pageable pageable);

    @Query("""
            select case when count(maintenanceWindow) > 0 then true else false end
            from MaintenanceWindow maintenanceWindow
            where maintenanceWindow.aircraft.aircraftId = :aircraftId
              and maintenanceWindow.status = :activeStatus
              and maintenanceWindow.startsAt < :candidateEnd
              and maintenanceWindow.endsAt > :candidateStart
            """)
    boolean existsOverlappingWindow(
            @Param("aircraftId") Long aircraftId,
            @Param("activeStatus") MaintenanceStatus activeStatus,
            @Param("candidateStart") OffsetDateTime candidateStart,
            @Param("candidateEnd") OffsetDateTime candidateEnd);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select window from MaintenanceWindow window where window.maintenanceWindowId = :windowId")
    Optional<MaintenanceWindow> findByIdForUpdate(@Param("windowId") Long windowId);
}
