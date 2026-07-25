package com.aeroops.springdemo.controller;

import com.aeroops.springdemo.dto.EmployeeRequestDTO;
import com.aeroops.springdemo.dto.EmployeeResponseDTO;
import com.aeroops.springdemo.entity.Employee;
import com.aeroops.springdemo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController
{
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService)
    {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployee(Pageable pageable)
    {
        return ResponseEntity.ok(employeeService.getEmployees(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id)
    {
        return new ResponseEntity<>(employeeService.getEmployeeById(id),HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto)
    {
        EmployeeResponseDTO request = employeeService.addEmployee(dto);
        return new ResponseEntity<>(request, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto)
    {
        return new ResponseEntity<>(employeeService.updateData(id, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id)
    {
        employeeService.deleteEmployeeById(id);

        return new ResponseEntity<>("Employee ID "+id+" deleted Succesfully",HttpStatus.OK);
    }
}