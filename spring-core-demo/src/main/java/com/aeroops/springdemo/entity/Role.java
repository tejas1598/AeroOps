package com.aeroops.springdemo.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Role
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;
    private  String roleName;
    @OneToMany(mappedBy = "role")
    private List<Employee> employees;

    public Role(){}

    public Role(Long roleId, String roleName, List<Employee> employees) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.employees = employees;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Employee> getEmployee() {
        return employees;
    }

    public void setEmployee(List<Employee> employees) {
        this.employees = employees;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", employee=" + employees +
                '}';
    }
}
