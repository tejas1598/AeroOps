package com.aeroops.springdemo.mapper;

import com.aeroops.springdemo.dto.DepartmentRequestDTO;
import com.aeroops.springdemo.dto.DepartmentResponseDTO;
import com.aeroops.springdemo.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper
{
    public Department toEntity(DepartmentRequestDTO dto)
    {
        Department department = new Department();

        department.setDepartmentName(dto.getDepartmentName());
        department.setDepartmentLocation(dto.getDepartmentLocation());

        return department;
    }

    public DepartmentResponseDTO toReponse(Department department)
    {
        DepartmentResponseDTO dto = new DepartmentResponseDTO();

        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setDepartmentLocation(department.getDepartmentLocation());

        return dto;
    }
}
