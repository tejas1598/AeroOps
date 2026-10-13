package com.aeroops.springdemo.security;

import com.aeroops.springdemo.entity.Authority;
import com.aeroops.springdemo.entity.Role;
import com.aeroops.springdemo.entity.User;
import com.aeroops.springdemo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserDetailService implements UserDetailsService
{
    private UserRepository userRepository;

    UserDetailService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Transactional()
    public CustomerUserDetails loadUserByUsername(String username)
    {
        Optional<User> user = userRepository.findByUserName(username);

        if(user.isEmpty())
        {
            throw new UsernameNotFoundException("User Not Found");
        }

        String userName = user.get().getUserName();
        String password = user.get().getPassword();
        User aeroUser = user.get();

        Role role = aeroUser.getRole();

        Set<GrantedAuthority> authorities = new HashSet<>();

        for(Authority a : role.getAuthorities())
        {
            authorities.add(new SimpleGrantedAuthority(a.getAuthorityName()));
        }

        return new CustomerUserDetails(userName, password, authorities);
    }
}


// just created the userDetail side, as we called the username as per the login details and called the user details in userDetails, with the authorities as granted authorities
// now we are at the stage where we had to check the authorization by means of userDetails and get the authorization confirmed