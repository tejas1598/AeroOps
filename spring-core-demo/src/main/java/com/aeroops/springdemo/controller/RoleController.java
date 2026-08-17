package com.aeroops.springdemo.controller;

import com.aeroops.springdemo.dto.RoleRequestDTO;
import com.aeroops.springdemo.dto.RoleResponseDTO;
import com.aeroops.springdemo.entity.Role;
import com.aeroops.springdemo.service.EmployeeService;
import com.aeroops.springdemo.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role")
public class RoleController
{
    private final RoleService roleService;
    public RoleController(RoleService roleService)
    {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponseDTO> displayAllRoles()
    {
        return roleService.getRole();
    }

    @GetMapping("/{id}")
    public RoleResponseDTO displayRoleById(@PathVariable Long id)
    {
        RoleResponseDTO role = roleService.getRoleById(id);
        return role;
    }

    @PostMapping
    public void addRole(@RequestBody RoleRequestDTO dto)
    {
        roleService.saveRole(dto);
    }

    @PutMapping("/{id}")
    public String updateRole(@PathVariable Long id, @RequestBody RoleRequestDTO dto)
    {
        String message = roleService.updateRoleById(id, dto);
        return message;
    }
}