package com.aeroops.springdemo.security;
import com.aeroops.springdemo.dto.ErrorResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private ObjectMapper objectMapper;

    CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        response.setContentType("application/json");

        ErrorResponseDTO errorResponsedto = new ErrorResponseDTO();

        errorResponsedto.setTimestamp(LocalDateTime.now());
        errorResponsedto.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponsedto.setMessage(authException.getMessage());
        errorResponsedto.setErrors(null);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponsedto));
    }
}
