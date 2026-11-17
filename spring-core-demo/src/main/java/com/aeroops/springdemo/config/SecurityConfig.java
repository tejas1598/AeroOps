package com.aeroops.springdemo.config;

import com.aeroops.springdemo.security.CustomAccessDeniedHandler;
import com.aeroops.springdemo.security.CustomAuthenticationEntryPoint;
import com.aeroops.springdemo.security.JwtAuthenticationFilter;
import com.aeroops.springdemo.security.UserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter)
    {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                         auth -> auth
                                 .requestMatchers(POST, "/auth/login")
                                 .permitAll()

                                 .requestMatchers(POST, "/api/v1/employees")
                                 .hasAuthority("EMPLOYEE_CREATE")

                                 .requestMatchers(DELETE, "/api/v1/employees/**")
                                 .hasAuthority("EMPLOYEE_DELETE")

                                 .requestMatchers(GET, "/api/v1/employees")
                                 .hasAuthority("EMPLOYEE_READ")

                                 .requestMatchers(GET, "/api/v1/aircraft", "/api/v1/aircraft/**")
                                 .hasAuthority("AIRCRAFT_READ")

                                 .requestMatchers(POST, "/api/v1/aircraft")
                                 .hasAuthority("AIRCRAFT_CREATE")

                                 .requestMatchers(PUT, "/api/v1/aircraft/**")
                                 .hasAuthority("AIRCRAFT_UPDATE")

                                 .requestMatchers(PATCH, "/api/v1/aircraft/*/status")
                                 .hasAuthority("AIRCRAFT_STATUS_UPDATE")

                                 .requestMatchers(GET, "/api/v1/airports", "/api/v1/airports/**")
                                 .hasAuthority("AIRPORT_READ")

                                 .requestMatchers(POST, "/api/v1/airports")
                                 .hasAuthority("AIRPORT_CREATE")

                                 .requestMatchers(PUT, "/api/v1/airports/**")
                                 .hasAuthority("AIRPORT_UPDATE")

                                 .requestMatchers(GET, "/api/v1/flights", "/api/v1/flights/**")
                                 .hasAuthority("FLIGHT_READ")

                                 .requestMatchers(POST, "/api/v1/flights")
                                 .hasAuthority("FLIGHT_SCHEDULE")

                                 .requestMatchers(PUT, "/api/v1/flights/*/aircraft")
                                 .hasAuthority("FLIGHT_ASSIGN")

                                 .requestMatchers(PATCH, "/api/v1/flights/*/status")
                                 .hasAuthority("FLIGHT_STATUS_UPDATE")

                                 .requestMatchers(GET, "/api/v1/maintenance-windows", "/api/v1/maintenance-windows/**")
                                 .hasAuthority("MAINTENANCE_READ")

                                 .requestMatchers(POST, "/api/v1/maintenance-windows")
                                 .hasAuthority("MAINTENANCE_MANAGE")

                                 .requestMatchers(PATCH, "/api/v1/maintenance-windows/*/cancel")
                                 .hasAuthority("MAINTENANCE_MANAGE")

                                 .requestMatchers(PUT, "/api/v1/flights/**")
                                 .hasAuthority("FLIGHT_UPDATE")

                                 .requestMatchers(PUT, "/api/v1/employees/**")
                                 .hasAuthority("EMPLOYEE_UPDATE")

                                 .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailService userDetailService, PasswordEncoder passwordEncoder)
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager getAuthenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
