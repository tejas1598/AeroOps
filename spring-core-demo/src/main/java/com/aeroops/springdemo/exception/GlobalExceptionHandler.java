package com.aeroops.springdemo.exception;

import com.aeroops.springdemo.dto.ErrorResponseDTO;
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
}
