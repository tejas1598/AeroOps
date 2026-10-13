package com.aeroops.springdemo.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Role
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;
    @Column(unique = true, nullable = false)
    private  String roleName;
    @OneToMany(mappedBy = "role")
    private List<Employee> employees;
    @ManyToMany
    private Set<Authority> authorities = new HashSet<>();
    @OneToMany(mappedBy = "role")
    private List<User> users;

    public Role(Long roleId, String roleName, List<Employee> employees, Set<Authority> authorities, List<User> users) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.employees = employees;
        this.authorities = authorities;
        this.users = users;
    }

    public Role(){}

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
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
