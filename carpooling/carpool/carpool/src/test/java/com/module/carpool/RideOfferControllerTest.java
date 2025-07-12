package com.module.carpool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.module.carpool.model.RideOffer;
import com.module.carpool.model.RideRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RideOfferControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private RideOffer sampleRide;
    private String companyUserId = "EMP1001";
    private String otherUserId = "EMP2002";

    @BeforeEach
    void setup() throws Exception {
        // Clean up by deleting all rides (if persistent storage is added in future,
        // this should be improved)
        // For in-memory, restart resets state
        sampleRide = new RideOffer();
        sampleRide.setStartPoint("Delhi");
        sampleRide.setStops(List.of("Stop1", "Stop2"));
        sampleRide.setDestination("Noida");
        sampleRide.setDateTime(LocalDateTime.now().plusDays(1));
        sampleRide.setAvailableSeats(2);
        sampleRide.setCompanyUserId(companyUserId);
        // Create a ride
        String rideJson = objectMapper.writeValueAsString(sampleRide);
        String response = mockMvc.perform(post("/rides/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rideJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        RideOffer created = objectMapper.readValue(response, RideOffer.class);
        sampleRide.setId(created.getId());
    }

    @Test
    void testCreateAndListRide() throws Exception {
        mockMvc.perform(get("/rides/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startPoint", is("Delhi")))
                .andExpect(jsonPath("$[0].availableSeats", is(2)));
    }

    @Test
    void testEditRide() throws Exception {
        sampleRide.setAvailableSeats(5);
        mockMvc.perform(put("/rides/" + sampleRide.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRide)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats", is(5)));
    }

    @Test
    void testDeleteRide() throws Exception {
        mockMvc.perform(delete("/rides/" + sampleRide.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rides/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testSearchRide() throws Exception {
        mockMvc.perform(get("/rides/search?startPoint=delhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destination", is("Noida")));
    }

    @Test
    void testRequestToJoinRide() throws Exception {
        RideRequest req = new RideRequest();
        req.setCompanyUserId(otherUserId);
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void testPreventDuplicateRequests() throws Exception {
        RideRequest req = new RideRequest();
        req.setCompanyUserId(otherUserId);
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testPreventRequestingOwnRide() throws Exception {
        RideRequest req = new RideRequest();
        req.setCompanyUserId(companyUserId);
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAcceptRequestAndOverbooking() throws Exception {
        RideRequest req1 = new RideRequest();
        req1.setCompanyUserId("EMP3003");
        RideRequest req2 = new RideRequest();
        req2.setCompanyUserId("EMP4004");
        // Add two requests
        String resp1 = mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andReturn().getResponse().getContentAsString();
        String resp2 = mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andReturn().getResponse().getContentAsString();
        RideRequest r1 = objectMapper.readValue(resp1, RideRequest.class);
        RideRequest r2 = objectMapper.readValue(resp2, RideRequest.class);
        // Accept both
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests/" + r1.getId() + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests/" + r2.getId() + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
        // Try to accept a third request (should fail, overbooking)
        RideRequest req3 = new RideRequest();
        req3.setCompanyUserId("EMP5005");
        String resp3 = mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req3)))
                .andReturn().getResponse().getContentAsString();
        RideRequest r3 = objectMapper.readValue(resp3, RideRequest.class);
        mockMvc.perform(post("/rides/" + sampleRide.getId() + "/requests/" + r3.getId() + "/accept"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testHideExpiredRides() throws Exception {
        RideOffer expiredRide = new RideOffer();
        expiredRide.setStartPoint("OldCity");
        expiredRide.setStops(List.of("StopA"));
        expiredRide.setDestination("PastTown");
        expiredRide.setDateTime(LocalDateTime.now().minusDays(1));
        expiredRide.setAvailableSeats(1);
        expiredRide.setCompanyUserId("EMP9999");
        mockMvc.perform(post("/rides/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expiredRide)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rides/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.startPoint=='OldCity')]").doesNotExist());
    }
}