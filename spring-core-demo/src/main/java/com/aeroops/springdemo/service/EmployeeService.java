package com.aeroops.springdemo.service;

import com.aeroops.springdemo.dto.EmployeeRequestDTO;
import com.aeroops.springdemo.dto.EmployeeResponseDTO;
import com.aeroops.springdemo.entity.Employee;
import com.aeroops.springdemo.exception.EmployeeNotFoundException;
import com.aeroops.springdemo.mapper.EmployeeMapper;
import com.aeroops.springdemo.repository.EmployeeRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    private final EmployeeMapper employeeMapper;


    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper=employeeMapper;
    }

    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO employeeRequestDTO)
    {
        Employee employee1 = employeeMapper.toEntity(employeeRequestDTO);
        employeeRepository.save(employee1);
        return employeeMapper.toResponseDTO(employee1);
    }

    public List<EmployeeResponseDTO> getEmployees()
    {
        List<Employee> employeeList = employeeRepository.findAll();
        List<EmployeeResponseDTO> reponseDTO =  new ArrayList<>();

        for(Employee employee : employeeList)
        {
            reponseDTO.add(employeeMapper.toResponseDTO(employee));
        }

        return reponseDTO;
    }

    public EmployeeResponseDTO getEmployeeById(Long id)
    {
        Optional<Employee> employee = employeeRepository.findById(id);
        if(employee.isEmpty())
        {
            throw new EmployeeNotFoundException("Employee with ID "+id+" not found");
        }
        Employee employee1 = employee.get();
        EmployeeResponseDTO dto = employeeMapper.toResponseDTO(employee1);
        return dto;
    }

    public EmployeeResponseDTO updateData(Long id, EmployeeRequestDTO dto) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);

        if (optionalEmployee.isPresent())
        {
            Employee employeeCopy = optionalEmployee.get();
            employeeCopy.setName(dto.getName());
            employeeCopy.setAge(dto.getAge());
            employeeRepository.save(employeeCopy);
        }
        else
        {
            return null;
        }

        return employeeMapper.toResponseDTO(optionalEmployee.get());
    }

    public void deleteEmployeeById(Long id)
    {
        Employee employee = employeeRepository.findById(id).orElse(null);

        if(employee != null)
            employeeRepository.deleteById(id);
        else
        {
            System.out.println("Employee with id " + id + " not found");
        }
    }

    @PreDestroy
    public void destroy()
    {
        System.out.println("EmployeeService Bean destroyed");
    }
}
