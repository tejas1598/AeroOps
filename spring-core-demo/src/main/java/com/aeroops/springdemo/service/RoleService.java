package com.aeroops.springdemo.service;

import com.aeroops.springdemo.dto.RoleRequestDTO;
import com.aeroops.springdemo.dto.RoleResponseDTO;
import com.aeroops.springdemo.entity.Role;
import com.aeroops.springdemo.exception.RoleNotFoundException;
import com.aeroops.springdemo.mapper.RoleMapper;
import com.aeroops.springdemo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService
{
    final private RoleRepository roleRepository;
    final private RoleMapper roleMapper;
    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper)
    {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    public List<RoleResponseDTO> getRole()
    {
        List<RoleResponseDTO> roleResponseDTO = new ArrayList<>();
        List<Role> role = roleRepository.findAll();
        for(Role role1 : role)
        {
            roleResponseDTO.add(roleMapper.toDTO(role1));
        }

        return roleResponseDTO;
    }

    public RoleResponseDTO getRoleById(Long id)
    {
        Role role = roleRepository.findById(id).orElseThrow(()->new RoleNotFoundException("Role not found"));
        RoleResponseDTO roleResponseDTO = roleMapper.toDTO(role);
        return roleResponseDTO;
    }

    public String saveRole(RoleRequestDTO dto)
    {
        roleRepository.save(roleMapper.toEntity(dto));
        return "Role saved successfully";
    }

    public String updateRoleById(Long id, RoleRequestDTO dto)
    {
        Role role = roleRepository.findById(id).orElseThrow(()->new RoleNotFoundException("Role not found"));
        role.setRoleName(dto.getRoleName());
        return "Role updated successfully";
    }
}
