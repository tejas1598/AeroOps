package com.aeroops.springdemo.specification;

import com.aeroops.springdemo.entity.Department;
import com.aeroops.springdemo.entity.Employee;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class EmployeeSpecification
{
    public Specification<Employee> hasStatus(String status)
    {
        return (root, query, builder) ->
        {
            if(status == null)
            {
                return null;
            }

            return builder.equal(root.get("status"), status);
        };
    }

    public Specification<Employee> hasDepartment(Long departmentId)
    {
        return (root, query, builder) ->
        {
          if(departmentId == null)
          {
              return null;
          }

          return builder.equal(root.get("department").get("departmentId"), departmentId);
        };
    }

    public Specification<Employee> hasRole(Long roleId)
    {
        return(root, query, builder) ->
        {
            if(roleId == null)
            {
                return null;
            }

            return builder.equal(root.get("role").get("roleId"), roleId);
        };
    }
}
