package com.module.carpool.model;

import lombok.Data;

@Data
public class RideRequest {
    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }

    private Long id;
    private Long rideId;
    private String passengerName;
    private Status status = Status.PENDING;
    private String companyUserId;
    private String realName; // Only for history
}