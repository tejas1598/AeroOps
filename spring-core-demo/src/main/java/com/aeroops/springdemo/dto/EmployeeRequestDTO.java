package com.aeroops.springdemo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeRequestDTO
{
    @NotBlank(message = "Employee name cannot be empty")
    @Size(min = 5, max = 20, message = "Name should be atleast of 5 letters and less than 20 letters")
    private String name;

    @Min(value = 18, message = "Age should be greater than 18")
    @Max(value = 60, message = "Age should be less than 60")
    private int age;

    public EmployeeRequestDTO(){}

    public EmployeeRequestDTO(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
