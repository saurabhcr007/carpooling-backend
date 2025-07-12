package com.module.carpool.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
public class RideOffer {
    private Long id;
    private String startPoint;
    private List<String> stops;
    private String destination;
    private LocalDateTime dateTime;
    private int availableSeats;
    private List<RideRequest> requests = new ArrayList<>();
    private String companyUserId;
    private String realName; // Only for history
}