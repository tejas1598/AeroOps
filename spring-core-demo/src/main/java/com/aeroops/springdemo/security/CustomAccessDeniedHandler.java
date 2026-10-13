package com.aeroops.springdemo.security;

import com.aeroops.springdemo.dto.ErrorResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler
{
    private ObjectMapper objectMapper;

    CustomAccessDeniedHandler(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException
    {
        response.setStatus(HttpStatus.FORBIDDEN.value());

        response.setContentType("application/json");

        ErrorResponseDTO errorResponsedto = new ErrorResponseDTO();
        errorResponsedto.setTimestamp(LocalDateTime.now());
        errorResponsedto.setStatus(HttpStatus.FORBIDDEN.value());
        errorResponsedto.setMessage(accessDeniedException.getMessage());
        errorResponsedto.setErrors(null);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponsedto));
    }
}