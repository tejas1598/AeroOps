package com.aeroops.springdemo.controller;

import com.aeroops.springdemo.dto.DepartmentRequestDTO;
import com.aeroops.springdemo.dto.DepartmentResponseDTO;
import com.aeroops.springdemo.entity.Department;
import com.aeroops.springdemo.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/department")
public class DepartmentController
{
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService)
    {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<DepartmentResponseDTO> getDeparments()
    {
        return departmentService.getDepartment();
    }

    @PostMapping
    public void addDepartment(@RequestBody DepartmentRequestDTO departmentReuqestDTO)
    {
        departmentService.addDepartment(departmentReuqestDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteDepartment(@PathVariable Long id)
    {
        departmentService.deleteDepartment(id);
    }

    @PutMapping("/{id}")
    public String updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequestDTO departmentReuqestDTO)
    {
        String message = departmentService.updateDepartmentById(id, departmentReuqestDTO);
        return message;
    }
}
