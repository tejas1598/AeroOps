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

        Role adminRole = getOrCreateRole("ADMIN");
        adminRole.getAuthorities().add(createAuthority);
        adminRole.getAuthorities().add(readAuthority);
        adminRole.getAuthorities().add(updateAuthority);
        adminRole.getAuthorities().add(deleteAuthority);
        roleRepository.save(adminRole);

        Role managerRole = getOrCreateRole("MANAGER");
        managerRole.getAuthorities().add(createAuthority);
        managerRole.getAuthorities().add(readAuthority);
        roleRepository.save(managerRole);

        Role employeeRole = getOrCreateRole("EMPLOYEE");
        employeeRole.getAuthorities().add(readAuthority);
        roleRepository.save(employeeRole);

        User user = getOrCreateUser("Aayush", "MANAGER", "123456");
        userRepository.save(user);

        User user1 = getOrCreateUser("Yash", "EMPLOYEE", "654321");
        userRepository.save(user1);

        User user2 = getOrCreateUser("Tejas", "ADMIN", "123654");
        userRepository.save(user2);
    }
}
