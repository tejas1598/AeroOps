package com.aeroops.springdemo.exception;

import com.aeroops.springdemo.dto.ErrorResponseDTO;
import com.aeroops.springdemo.aircraft.exception.AircraftAlreadyExistsException;
import com.aeroops.springdemo.aircraft.exception.AircraftMaintenanceConflictException;
import com.aeroops.springdemo.aircraft.exception.AircraftNotFoundException;
import com.aeroops.springdemo.aircraft.exception.AircraftScheduleConflictException;
import com.aeroops.springdemo.aircraft.exception.AircraftUnavailableException;
import com.aeroops.springdemo.airport.exception.AirportAlreadyExistsException;
import com.aeroops.springdemo.airport.exception.AirportNotFoundException;
import com.aeroops.springdemo.flight.exception.FlightAlreadyExistsException;
import com.aeroops.springdemo.flight.exception.FlightNotFoundException;
import com.aeroops.springdemo.flight.exception.FlightNotAssignableException;
import com.aeroops.springdemo.flight.exception.InvalidFlightScheduleException;
import com.aeroops.springdemo.flight.exception.InvalidFlightStatusTransitionException;
import com.aeroops.springdemo.maintenance.exception.InvalidMaintenanceWindowException;
import com.aeroops.springdemo.maintenance.exception.MaintenanceWindowConflictException;
import com.aeroops.springdemo.maintenance.exception.MaintenanceWindowNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotValidException(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();
        List<FieldError> fieldError = ex.getBindingResult().getFieldErrors();

        for(FieldError fieldError1 : fieldError)
        {
            errors.put(fieldError1.getField(), fieldError1.getDefaultMessage());
        }

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();

        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponseDTO.setErrors(errors);
        errorResponseDTO.setMessage("Validation Failed");

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST); //OR IT CAN BE - return ResponseEntity.badRequest().body(errorResponseDTO);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> employeeNotFoundException(EmployeeNotFoundException ex)
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();

        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponseDTO.setMessage(ex.getMessage());
        errorResponseDTO.setErrors(null);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> departmentNotFoundException(DepartmentNotFoundException ex)
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();

        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponseDTO.setMessage(ex.getMessage());
        errorResponseDTO.setErrors(null);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> roleNotFoundException(RoleNotFoundException ex)
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();

        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponseDTO.setMessage(ex.getMessage());
        errorResponseDTO.setErrors(null);

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AircraftNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> aircraftNotFoundException(AircraftNotFoundException ex)
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponseDTO.setMessage(ex.getMessage());
        errorResponseDTO.setErrors(null);
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AircraftAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> aircraftAlreadyExistsException(AircraftAlreadyExistsException ex)
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(HttpStatus.CONFLICT.value());
        errorResponseDTO.setMessage(ex.getMessage());
        errorResponseDTO.setErrors(null);
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> airportNotFoundException(AirportNotFoundException ex)
    {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AirportAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> airportAlreadyExistsException(AirportAlreadyExistsException ex)
    {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> flightNotFoundException(FlightNotFoundException ex)
    {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(FlightAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> flightAlreadyExistsException(FlightAlreadyExistsException ex)
    {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidFlightScheduleException.class)
    public ResponseEntity<ErrorResponseDTO> invalidFlightScheduleException(InvalidFlightScheduleException ex)
    {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidFlightStatusTransitionException.class)
    public ResponseEntity<ErrorResponseDTO> invalidFlightStatusTransition(InvalidFlightStatusTransitionException ex)
    {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({AircraftUnavailableException.class, AircraftScheduleConflictException.class,
            AircraftMaintenanceConflictException.class, FlightNotAssignableException.class,
            MaintenanceWindowConflictException.class})
    public ResponseEntity<ErrorResponseDTO> aircraftAssignmentConflict(RuntimeException ex)
    {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MaintenanceWindowNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> maintenanceWindowNotFound(MaintenanceWindowNotFoundException ex)
    {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidMaintenanceWindowException.class)
    public ResponseEntity<ErrorResponseDTO> invalidMaintenanceWindow(InvalidMaintenanceWindowException ex)
    {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ErrorResponseDTO> errorResponse(HttpStatus status, String message)
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setTimestamp(LocalDateTime.now());
        errorResponseDTO.setStatus(status.value());
        errorResponseDTO.setMessage(message);
        errorResponseDTO.setErrors(null);
        return new ResponseEntity<>(errorResponseDTO, status);
    }
}
