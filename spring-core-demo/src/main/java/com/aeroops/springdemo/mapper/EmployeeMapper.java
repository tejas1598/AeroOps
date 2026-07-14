package com.aeroops.springdemo.mapper;

import com.aeroops.springdemo.dto.EmployeeRequestDTO;
import com.aeroops.springdemo.dto.EmployeeResponseDTO;
import com.aeroops.springdemo.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper
{
    public Employee toEntity(EmployeeRequestDTO dto)
    {
        Employee employee = new Employee();

        employee.setName(dto.getName());
        employee.setAge(dto.getAge());
        return employee;
    }

    public EmployeeResponseDTO toResponseDTO(Employee employee)
    {
        EmployeeResponseDTO employee_res = new EmployeeResponseDTO();

        employee_res.setId(employee.getId());
        employee_res.setName(employee.getName());
        employee_res.setAge(employee.getAge());
        return employee_res;
    }
}
