package com.aeroops.springdemo.dto;

public class RoleRequestDTO
{
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "RoleRequestDTO{" +
                "roleName='" + roleName + '\'' +
                '}';
    }
}
