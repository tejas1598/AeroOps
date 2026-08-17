package com.aeroops.springdemo.repository;

import com.aeroops.springdemo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>
{
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e " +
            "SET e.department = NULL " +
            "WHERE e.department.departmentId = :departmentId")
    int clearDepartment(@Param("departmentId") int departmentId);
}
