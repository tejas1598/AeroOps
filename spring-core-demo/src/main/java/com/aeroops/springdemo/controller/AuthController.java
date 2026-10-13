package com.aeroops.springdemo.controller;

import com.aeroops.springdemo.dto.LoginRequestDTO;
import com.aeroops.springdemo.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
public class AuthController
{
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    AuthController(AuthenticationManager authenticationManager,  JwtService jwtService)
    {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    public String login(@RequestBody LoginRequestDTO loginRequestDTO)
    {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getUsername(),
                loginRequestDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        return jwtService.generateToken(authentication);
    }
}
