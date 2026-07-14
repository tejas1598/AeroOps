package com.aeroops.springdemo.controller;

import com.aeroops.springdemo.dto.EmployeeRequestDTO;
import com.aeroops.springdemo.dto.EmployeeResponseDTO;
import com.aeroops.springdemo.entity.Employee;
import com.aeroops.springdemo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController
{
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService)
    {
        this.employeeService = employeeService;
    }

    @PostMapping
    public void createEmployee(@Valid @RequestBody EmployeeRequestDTO dto)
    {
        employeeService.addEmployee(dto);
    }

    @GetMapping
    public List<Employee> getEmployee()
    {
        return employeeService.getEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponseDTO getEmployeeById(@PathVariable Long id)
    {
        return employeeService.getEmployeeById(id);
    }

    @PutMapping("/{id}")
    public void updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto)
    {
        employeeService.updateData(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id)
    {
        employeeService.deleteEmployeeById(id);
    }
}
