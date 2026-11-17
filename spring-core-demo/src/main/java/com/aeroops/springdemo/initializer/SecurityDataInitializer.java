package com.aeroops.springdemo.initializer;

import com.aeroops.springdemo.entity.Authority;
import com.aeroops.springdemo.entity.Role;
import com.aeroops.springdemo.entity.User;
import com.aeroops.springdemo.repository.AuthorityRepository;
import com.aeroops.springdemo.repository.RoleRepository;
import com.aeroops.springdemo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityDataInitializer implements CommandLineRunner
{
    private AuthorityRepository authorityRepository;
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public SecurityDataInitializer(AuthorityRepository authorityRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserRepository userRepository)
    {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public Authority getOrCreateAuthority(String authorityName)
    {
        Optional<Authority> existingAuthority = authorityRepository.findByAuthorityName(authorityName);
        if(existingAuthority.isPresent())
        {
            return existingAuthority.get();
        }
        else
        {
            Authority authority = new Authority();
            authority.setAuthorityName(authorityName);
            return authorityRepository.save(authority);
        }
    }

    public Role getOrCreateRole(String roleName)
    {
        Optional<Role> existingRole = roleRepository.findByRoleName(roleName);

        if(existingRole.isPresent())
        {
            return existingRole.get();
        }
        else
        {
            Role role = new Role();
            role.setRoleName(roleName);
            return roleRepository.save(role);
        }
    }

    public User getOrCreateUser(String username, String roleName, String password)
    {
        Optional<User> user = userRepository.findByUserName(username);

        if(user.isPresent())
        {
            return user.get();
        }
        else {
            User user1 = new User();
            user1.setUserName(username);
            user1.setPassword(passwordEncoder.encode(password));
            user1.setRole(getOrCreateRole(roleName));
            return userRepository.save(user1);
        }
    }

    @Override
    @Transactional
    public void run(String... args)
    {
        Authority createAuthority = getOrCreateAuthority("EMPLOYEE_CREATE");
        Authority readAuthority = getOrCreateAuthority("EMPLOYEE_READ");
        Authority updateAuthority = getOrCreateAuthority("EMPLOYEE_UPDATE");
        Authority deleteAuthority = getOrCreateAuthority("EMPLOYEE_DELETE");
        Authority aircraftReadAuthority = getOrCreateAuthority("AIRCRAFT_READ");
        Authority aircraftCreateAuthority = getOrCreateAuthority("AIRCRAFT_CREATE");
        Authority aircraftUpdateAuthority = getOrCreateAuthority("AIRCRAFT_UPDATE");
        Authority aircraftStatusUpdateAuthority = getOrCreateAuthority("AIRCRAFT_STATUS_UPDATE");
        Authority airportReadAuthority = getOrCreateAuthority("AIRPORT_READ");
        Authority airportCreateAuthority = getOrCreateAuthority("AIRPORT_CREATE");
        Authority airportUpdateAuthority = getOrCreateAuthority("AIRPORT_UPDATE");
        Authority flightReadAuthority = getOrCreateAuthority("FLIGHT_READ");
        Authority flightScheduleAuthority = getOrCreateAuthority("FLIGHT_SCHEDULE");
        Authority flightUpdateAuthority = getOrCreateAuthority("FLIGHT_UPDATE");
        Authority flightAssignAuthority = getOrCreateAuthority("FLIGHT_ASSIGN");
        Authority flightStatusUpdateAuthority = getOrCreateAuthority("FLIGHT_STATUS_UPDATE");
        Authority maintenanceReadAuthority = getOrCreateAuthority("MAINTENANCE_READ");
        Authority maintenanceManageAuthority = getOrCreateAuthority("MAINTENANCE_MANAGE");

        Role adminRole = getOrCreateRole("ADMIN");
        adminRole.getAuthorities().add(createAuthority);
        adminRole.getAuthorities().add(readAuthority);
        adminRole.getAuthorities().add(updateAuthority);
        adminRole.getAuthorities().add(deleteAuthority);
        adminRole.getAuthorities().add(aircraftReadAuthority);
        adminRole.getAuthorities().add(aircraftCreateAuthority);
        adminRole.getAuthorities().add(aircraftUpdateAuthority);
        adminRole.getAuthorities().add(aircraftStatusUpdateAuthority);
        adminRole.getAuthorities().add(airportReadAuthority);
        adminRole.getAuthorities().add(airportCreateAuthority);
        adminRole.getAuthorities().add(airportUpdateAuthority);
        adminRole.getAuthorities().add(flightReadAuthority);
        adminRole.getAuthorities().add(flightScheduleAuthority);
        adminRole.getAuthorities().add(flightUpdateAuthority);
        adminRole.getAuthorities().add(flightAssignAuthority);
        adminRole.getAuthorities().add(flightStatusUpdateAuthority);
        adminRole.getAuthorities().add(maintenanceReadAuthority);
        adminRole.getAuthorities().add(maintenanceManageAuthority);
        roleRepository.save(adminRole);

        Role managerRole = getOrCreateRole("MANAGER");
        managerRole.getAuthorities().add(createAuthority);
        managerRole.getAuthorities().add(readAuthority);
        managerRole.getAuthorities().add(aircraftReadAuthority);
        managerRole.getAuthorities().add(airportReadAuthority);
        managerRole.getAuthorities().add(flightReadAuthority);
        managerRole.getAuthorities().add(flightScheduleAuthority);
        managerRole.getAuthorities().add(flightUpdateAuthority);
        managerRole.getAuthorities().add(flightAssignAuthority);
        managerRole.getAuthorities().add(flightStatusUpdateAuthority);
        managerRole.getAuthorities().add(maintenanceReadAuthority);
        roleRepository.save(managerRole);

        Role employeeRole = getOrCreateRole("EMPLOYEE");
        employeeRole.getAuthorities().add(readAuthority);
        employeeRole.getAuthorities().add(aircraftReadAuthority);
        employeeRole.getAuthorities().add(airportReadAuthority);
        employeeRole.getAuthorities().add(flightReadAuthority);
        roleRepository.save(employeeRole);

        Role maintenanceRole = getOrCreateRole("MAINTENANCE_TECH");
        maintenanceRole.getAuthorities().add(aircraftReadAuthority);
        maintenanceRole.getAuthorities().add(aircraftStatusUpdateAuthority);
        maintenanceRole.getAuthorities().add(airportReadAuthority);
        maintenanceRole.getAuthorities().add(flightReadAuthority);
        maintenanceRole.getAuthorities().add(maintenanceReadAuthority);
        maintenanceRole.getAuthorities().add(maintenanceManageAuthority);
        roleRepository.save(maintenanceRole);

        User user = getOrCreateUser("Aayush", "MANAGER", "123456");
        userRepository.save(user);

        User user1 = getOrCreateUser("Yash", "EMPLOYEE", "654321");
        userRepository.save(user1);

        User user2 = getOrCreateUser("Tejas", "ADMIN", "123654");
        userRepository.save(user2);

        User user3 = getOrCreateUser("Nakul", "MAINTENANCE_TECH", "123456");
        userRepository.save(user3);
    }
}
