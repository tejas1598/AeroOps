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
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setSalary(dto.getSalary());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setStatus(dto.getStatus());
        return employee;
    }

    public EmployeeResponseDTO toResponseDTO(Employee employee)
    {
        EmployeeResponseDTO employee_res = new EmployeeResponseDTO();

        employee_res.setEmployeeId(employee.getEmployeeId());
        employee_res.setFirstName(employee.getFirstName());
        employee_res.setLastName(employee.getLastName());
        employee_res.setEmail(employee.getEmail());
        employee_res.setDepartment(employee.getDepartment().getDepartmentName());
        employee_res.setRole(employee.getRole().getRoleName());
        employee_res.setStatus(employee.getStatus());

        return employee_res;
    }
}
