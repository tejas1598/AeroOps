package com.aeroops.springdemo;

import com.aeroops.springdemo.aircraft.repository.AircraftRepository;
import com.aeroops.springdemo.airport.repository.AirportRepository;
import com.aeroops.springdemo.flight.repository.FlightRepository;
import com.aeroops.springdemo.maintenance.repository.MaintenanceWindowRepository;
import com.aeroops.springdemo.repository.RoleRepository;
import com.aeroops.springdemo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AeroOpsWorkflowIntegrationTest {
    private static final String TEST_USERNAME = "aeroops-integration-test";
    private static final String TEST_PASSWORD = "integration-test-password";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private MaintenanceWindowRepository maintenanceWindowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String bearerToken;

    @BeforeEach
    void setUp() throws Exception {
        maintenanceWindowRepository.deleteAll();
        flightRepository.deleteAll();
        aircraftRepository.deleteAll();
        airportRepository.deleteAll();

        var adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();
        var testUser = userRepository.findByUserName(TEST_USERNAME)
                .orElseGet(() -> new com.aeroops.springdemo.entity.User());
        testUser.setUserName(TEST_USERNAME);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setRole(adminRole);
        userRepository.save(testUser);

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", TEST_USERNAME, "password", TEST_PASSWORD))))
                .andReturn();
        assertEquals(200, login.getResponse().getStatus());
        bearerToken = login.getResponse().getContentAsString();
        assertNotNull(bearerToken);
    }

    @Test
    void completeFleetFlightAndMaintenanceWorkflowThroughApi() throws Exception {
        Map<String, Object> aircraftRequest = Map.of(
                "registrationCode", "VT-INT", "manufacturer", "Airbus", "model", "A320", "seatingCapacity", 180);
        MvcResult aircraftResult = send(HttpMethod.POST, "/api/v1/aircraft", aircraftRequest);
        assertEquals(201, aircraftResult.getResponse().getStatus());
        Long aircraftId = read(aircraftResult).get("aircraftId").asLong();

        Long originAirportId = createAirport("BOM", "Mumbai Airport", "Mumbai");
        Long destinationAirportId = createAirport("DEL", "Delhi Airport", "Delhi");

        OffsetDateTime departure = OffsetDateTime.now().plusDays(5).withNano(0);
        Long flightId = createFlight("AI101", originAirportId, destinationAirportId,
                departure, departure.plusHours(2));

        Map<String, Object> maintenance = maintenanceRequest(
                aircraftId, departure.minusMinutes(30), departure.plusHours(1));
        MvcResult windowResult = send(HttpMethod.POST, "/api/v1/maintenance-windows", maintenance);
        assertEquals(201, windowResult.getResponse().getStatus());
        Long maintenanceWindowId = read(windowResult).get("maintenanceWindowId").asLong();

        assertEquals(409, assignAircraft(flightId, aircraftId).getResponse().getStatus());

        MvcResult cancelledWindow = send(HttpMethod.PATCH,
                "/api/v1/maintenance-windows/" + maintenanceWindowId + "/cancel", null);
        assertEquals(200, cancelledWindow.getResponse().getStatus());

        MvcResult assignment = send(HttpMethod.PUT, "/api/v1/flights/" + flightId + "/aircraft",
                Map.of("aircraftId", aircraftId));
        assertEquals(200, assignment.getResponse().getStatus());
        assertEquals(aircraftId, read(assignment).get("aircraftId").asLong());

        Long overlappingFlightId = createFlight("AI102", originAirportId, destinationAirportId,
                departure.plusMinutes(30), departure.plusHours(2).plusMinutes(30));
        assertEquals(409, assignAircraft(overlappingFlightId, aircraftId).getResponse().getStatus());

        Map<String, Object> conflictingMaintenance = maintenanceRequest(
                aircraftId, departure.plusMinutes(15), departure.plusHours(1));
        assertEquals(409, send(HttpMethod.POST, "/api/v1/maintenance-windows",
                conflictingMaintenance).getResponse().getStatus());

        assertEquals(200, updateFlightStatus(flightId, "BOARDING").getResponse().getStatus());
        Map<String, String> takeOutOfService = Map.of("status", "IN_MAINTENANCE");
        assertEquals(409, send(HttpMethod.PATCH, "/api/v1/aircraft/" + aircraftId + "/status",
                takeOutOfService).getResponse().getStatus());

        assertEquals(200, updateFlightStatus(flightId, "IN_AIR").getResponse().getStatus());
        assertEquals(200, updateFlightStatus(flightId, "LANDED").getResponse().getStatus());

        assertEquals(200, send(HttpMethod.PATCH, "/api/v1/aircraft/" + aircraftId + "/status",
                takeOutOfService).getResponse().getStatus());
    }

    private Long createAirport(String code, String name, String city) throws Exception {
        Map<String, String> requestBody = Map.of("iataCode", code, "name", name, "city", city, "countryCode", "IN");
        MvcResult result = send(HttpMethod.POST, "/api/v1/airports", requestBody);
        assertEquals(201, result.getResponse().getStatus());
        return read(result).get("airportId").asLong();
    }

    private Long createFlight(String number, Long originId, Long destinationId,
                              OffsetDateTime departure, OffsetDateTime arrival) throws Exception {
        Map<String, Object> requestBody = Map.of("flightNumber", number, "originAirportId", originId,
                "destinationAirportId", destinationId, "scheduledDeparture", departure.toString(),
                "scheduledArrival", arrival.toString());
        MvcResult result = send(HttpMethod.POST, "/api/v1/flights", requestBody);
        assertEquals(201, result.getResponse().getStatus());
        return read(result).get("flightId").asLong();
    }

    private MvcResult updateFlightStatus(Long flightId, String status) throws Exception {
        return send(HttpMethod.PATCH, "/api/v1/flights/" + flightId + "/status", Map.of("status", status));
    }

    private MvcResult assignAircraft(Long flightId, Long aircraftId) throws Exception {
        return send(HttpMethod.PUT, "/api/v1/flights/" + flightId + "/aircraft",
                Map.of("aircraftId", aircraftId));
    }

    private Map<String, Object> maintenanceRequest(Long aircraftId,
                                                   OffsetDateTime startsAt,
                                                   OffsetDateTime endsAt) {
        return Map.of("aircraftId", aircraftId, "startsAt", startsAt.toString(),
                "endsAt", endsAt.toString(), "reason", "Integration test inspection");
    }

    private MvcResult send(HttpMethod method, String path, Object body) throws Exception {
        MockHttpServletRequestBuilder request = request(method, path)
                .header("Authorization", "Bearer " + bearerToken)
                .accept(MediaType.APPLICATION_JSON);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(request).andReturn();
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
