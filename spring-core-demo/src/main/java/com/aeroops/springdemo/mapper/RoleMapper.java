package com.aeroops.springdemo.mapper;

import com.aeroops.springdemo.dto.RoleRequestDTO;
import com.aeroops.springdemo.dto.RoleResponseDTO;
import com.aeroops.springdemo.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper
{
    public Role toEntity(RoleRequestDTO dto)
    {
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        return role;
    }

    public RoleResponseDTO toDTO(Role role)
    {
        RoleResponseDTO roleResponseDTO = new RoleResponseDTO();
        roleResponseDTO.setRoleID(role.getRoleId());
        roleResponseDTO.setRoleName(role.getRoleName());
        return roleResponseDTO;
    }
}
