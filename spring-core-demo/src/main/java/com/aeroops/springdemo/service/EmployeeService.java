package com.aeroops.springdemo.service;

import com.aeroops.springdemo.dto.EmployeeRequestDTO;
import com.aeroops.springdemo.dto.EmployeeResponseDTO;
import com.aeroops.springdemo.entity.Department;
import com.aeroops.springdemo.entity.Employee;
import com.aeroops.springdemo.entity.Role;
import com.aeroops.springdemo.exception.DepartmentNotFoundException;
import com.aeroops.springdemo.exception.EmployeeNotFoundException;
import com.aeroops.springdemo.exception.RoleNotFoundException;
import com.aeroops.springdemo.mapper.EmployeeMapper;
import com.aeroops.springdemo.repository.DepartmentRepository;
import com.aeroops.springdemo.repository.EmployeeRepository;
import com.aeroops.springdemo.repository.RoleRepository;
import com.aeroops.springdemo.specification.EmployeeSpecification;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeSpecification employeeSpecification;

    public EmployeeService(DepartmentRepository departmentRepository,EmployeeRepository employeeRepository,
                           EmployeeMapper employeeMapper, RoleRepository roleRepository, EmployeeSpecification employeeSpecification) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper=employeeMapper;
        this.roleRepository=roleRepository;
        this.departmentRepository=departmentRepository;
        this.employeeSpecification=employeeSpecification;
    }

    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO employeeRequestDTO)
    {
        Employee employee1 = employeeMapper.toEntity(employeeRequestDTO);
        Department department = departmentRepository.findById(employeeRequestDTO.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department with ID "+ employeeRequestDTO.getDepartmentId()+" not found")
                );
        employee1.setDepartment(department);
        Role role = roleRepository.findById(employeeRequestDTO.getRoleId()).orElseThrow(() -> new RoleNotFoundException("Role with ID "+ employeeRequestDTO.getRoleId()+" not found")
        );

        System.out.println(employee1);
        employee1.setRole(role);
        Employee saveEmployee = employeeRepository.save(employee1);
        return employeeMapper.toResponseDTO(saveEmployee);
    }

    public Page<EmployeeResponseDTO> getEmployees(Pageable pageable, String status, Long departmentId, Long roleId)
    {
        if(status == null && departmentId == null && roleId == null)
        {
            Page<Employee> employeePage = employeeRepository.findAll(pageable);
            Page<EmployeeResponseDTO> pageResponseDTO = employeePage.map(employee -> employeeMapper.toResponseDTO(employee));
            return pageResponseDTO;
        }
        else
        {
            Specification<Employee> specificationData = employeeSpecification.hasStatus(status).and(employeeSpecification.hasDepartment(departmentId)).and(employeeSpecification.hasRole(roleId));
            Page<Employee> employeePage = employeeRepository.findAll(specificationData, pageable);
            return employeePage.map(employee -> employeeMapper.toResponseDTO(employee));
        }
    }

    public EmployeeResponseDTO getEmployeeById(Long id)
    {
        Optional<Employee> employee = employeeRepository.findById(id);
        if(employee.isEmpty())
        {
            throw new EmployeeNotFoundException("Employee with ID "+id+" not found");
        }
        EmployeeResponseDTO dto = employeeMapper.toResponseDTO(employee.get());
        return dto;
    }

    @Transactional
    public EmployeeResponseDTO updateData(Long id, EmployeeRequestDTO dto) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);

        if(optionalEmployee.isEmpty())
        {
            throw new EmployeeNotFoundException("Employee with ID "+id+" not found");
        }

        if (optionalEmployee.isPresent())
        {
            Employee employeeCopy = optionalEmployee.get();

            employeeCopy.setFirstName(dto.getFirstName());
            employeeCopy.setLastName(dto.getLastName());
            employeeCopy.setEmail(dto.getEmail());
            employeeCopy.setPhoneNumber(dto.getPhoneNumber());
            employeeCopy.setStatus(dto.getStatus());
            employeeCopy.setDepartment(departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(()->
                            new DepartmentNotFoundException(
                                    "Department with ID "+ dto.getDepartmentId()+" not found"
                            )
                    ));
            employeeCopy.setRole(roleRepository.findById(dto.getRoleId())
                    .orElseThrow(()->
                            new RoleNotFoundException(
                                    "Role with ID "+ dto.getRoleId()+" not found"
                            )
                    ));

            employeeRepository.save(employeeCopy);
        }

        return employeeMapper.toResponseDTO(optionalEmployee.get());
    }

    @Transactional
    public void deleteEmployeeById(Long id)
    {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new EmployeeNotFoundException("Employee with ID "+id+" not found")
        );

        employeeRepository.delete(employee);
    }

    @PreDestroy
    public void destroy()
    {
        System.out.println("EmployeeService Bean destroyed");
    }
}
