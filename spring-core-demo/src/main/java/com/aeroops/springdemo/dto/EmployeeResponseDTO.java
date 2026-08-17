package com.aeroops.springdemo.dto;

import com.aeroops.springdemo.entity.Department;
import com.aeroops.springdemo.entity.Role;

public class EmployeeResponseDTO
{
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String role;
    private String status;

    public EmployeeResponseDTO(Long employeeId, String firstName, String lastName,
                               String email, String Status) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
    }

    public EmployeeResponseDTO() {}

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
