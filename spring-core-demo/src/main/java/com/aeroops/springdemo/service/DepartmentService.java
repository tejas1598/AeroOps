package com.aeroops.springdemo.service;

import com.aeroops.springdemo.dto.DepartmentRequestDTO;
import com.aeroops.springdemo.dto.DepartmentResponseDTO;
import com.aeroops.springdemo.entity.Department;
import com.aeroops.springdemo.exception.DepartmentNotFoundException;
import com.aeroops.springdemo.exception.EmployeeNotFoundException;
import com.aeroops.springdemo.mapper.DepartmentMapper;
import com.aeroops.springdemo.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService
{
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper)
    {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public void addDepartment(DepartmentRequestDTO departmentReuqestDTO)
    {
        departmentRepository.save(departmentMapper.toEntity(departmentReuqestDTO));
    }

    public List<DepartmentResponseDTO> getDepartment()
    {
        List<Department> allDepartments = departmentRepository.findAll();

        List<DepartmentResponseDTO> departmentResponseDTO = new ArrayList<>();

        for(Department d : allDepartments)
        {
            departmentResponseDTO.add(departmentMapper.toReponse(d));
        }

        return departmentResponseDTO;
    }

    public void deleteDepartment(Long id)
    {
        Optional<Department> department = departmentRepository.findById(id);
        if(department.isEmpty())
        {
            throw new DepartmentNotFoundException("Department not found");
        }

        departmentRepository.deleteById(id);
    }

    public String updateDepartmentById(Long id, DepartmentRequestDTO departmentReuqestDTO)
    {
        Department department = departmentRepository.findById(id).orElseThrow(()->new DepartmentNotFoundException("Department not found"));

        department.setDepartmentName(departmentReuqestDTO.getDepartmentName());
        department.setDepartmentLocation(departmentReuqestDTO.getDepartmentLocation());

        return "Department updated successfully";
    }
}
