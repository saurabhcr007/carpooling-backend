package com.module.carpool.controller;

import com.module.carpool.model.RideOffer;
import com.module.carpool.model.RideRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/rides")
public class RideOfferController {
    private final List<RideOffer> rideOffers = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final AtomicLong requestIdGenerator = new AtomicLong();

    @PostMapping("/create")
    public RideOffer createRide(@RequestBody RideOffer rideOffer) {
        rideOffer.setId(idGenerator.incrementAndGet());
        rideOffers.add(rideOffer);
        return rideOffer;
    }

    @GetMapping("/all")
    public List<RideOffer> getAllRides() {
        // Only show rides with available seats and not expired
        LocalDateTime now = LocalDateTime.now();
        return rideOffers.stream()
                .filter(ride -> ride.getAvailableSeats() > 0)
                .filter(ride -> ride.getDateTime().isAfter(now))
                .toList();
    }

    @GetMapping("/search")
    public List<RideOffer> searchRides(
            @RequestParam(required = false) String startPoint,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date, // format: yyyy-MM-dd
            @RequestParam(required = false) String stop,
            @RequestParam(required = false) Integer minSeats) {
        LocalDateTime now = LocalDateTime.now();
        return rideOffers.stream()
                .filter(ride -> ride.getAvailableSeats() > 0)
                .filter(ride -> ride.getDateTime().isAfter(now))
                .filter(ride -> startPoint == null
                        || ride.getStartPoint().toLowerCase().contains(startPoint.toLowerCase()))
                .filter(ride -> destination == null
                        || ride.getDestination().toLowerCase().contains(destination.toLowerCase()))
                .filter(ride -> {
                    if (date == null)
                        return true;
                    try {
                        LocalDate filterDate = LocalDate.parse(date);
                        return ride.getDateTime().toLocalDate().equals(filterDate);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                })
                .filter(ride -> stop == null
                        || ride.getStops().stream().anyMatch(s -> s.toLowerCase().contains(stop.toLowerCase())))
                .filter(ride -> minSeats == null || ride.getAvailableSeats() >= minSeats)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteRide(@PathVariable Long id) {
        rideOffers.removeIf(ride -> ride.getId().equals(id));
    }

    @PutMapping("/{id}")
    public RideOffer updateRide(@PathVariable Long id, @RequestBody RideOffer updatedRide) {
        for (int i = 0; i < rideOffers.size(); i++) {
            RideOffer ride = rideOffers.get(i);
            if (ride.getId().equals(id)) {
                updatedRide.setId(id);
                rideOffers.set(i, updatedRide);
                return updatedRide;
            }
        }
        throw new RuntimeException("Ride not found");
    }

    @PostMapping("/{rideId}/requests")
    public RideRequest requestToJoin(@PathVariable Long rideId, @RequestBody RideRequest request) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        RideOffer ride = rideOpt.get();
        // Prevent requesting own ride
        if (ride.getCompanyUserId() != null && ride.getCompanyUserId().equals(request.getCompanyUserId())) {
            throw new RuntimeException("You cannot request to join your own ride.");
        }
        // Prevent duplicate requests
        boolean alreadyRequested = ride.getRequests().stream()
                .anyMatch(r -> r.getCompanyUserId() != null && r.getCompanyUserId().equals(request.getCompanyUserId()));
        if (alreadyRequested) {
            throw new RuntimeException("You have already requested to join this ride.");
        }
        request.setId(requestIdGenerator.incrementAndGet());
        request.setRideId(rideId);
        request.setStatus(RideRequest.Status.PENDING);
        ride.getRequests().add(request);
        return request;
    }

    @GetMapping("/{rideId}/requests")
    public List<RideRequest> getRequestsForRide(@PathVariable Long rideId) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        return rideOpt.get().getRequests();
    }

    @PostMapping("/{rideId}/requests/{requestId}/accept")
    public RideRequest acceptRequest(@PathVariable Long rideId, @PathVariable Long requestId) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        RideOffer ride = rideOpt.get();
        for (RideRequest req : ride.getRequests()) {
            if (req.getId().equals(requestId)) {
                // Prevent overbooking
                if (ride.getAvailableSeats() <= 0)
                    throw new RuntimeException("No seats available");
                req.setStatus(RideRequest.Status.ACCEPTED);
                ride.setAvailableSeats(ride.getAvailableSeats() - 1);
                return req;
            }
        }
        throw new RuntimeException("Request not found");
    }

    @PostMapping("/{rideId}/requests/{requestId}/reject")
    public RideRequest rejectRequest(@PathVariable Long rideId, @PathVariable Long requestId) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        RideOffer ride = rideOpt.get();
        for (RideRequest req : ride.getRequests()) {
            if (req.getId().equals(requestId)) {
                req.setStatus(RideRequest.Status.REJECTED);
                return req;
            }
        }
        throw new RuntimeException("Request not found");
    }

    @GetMapping("/history/offered")
    public List<RideOffer> getOfferedHistory(@RequestParam String companyUserId) {
        // Return all rides offered by this user, including real names of accepted
        // passengers
        return rideOffers.stream()
                .filter(ride -> ride.getCompanyUserId() != null && ride.getCompanyUserId().equals(companyUserId))
                .toList();
    }

    @GetMapping("/history/taken")
    public List<RideOffer> getTakenHistory(@RequestParam String companyUserId) {
        // Return all rides where this user has an accepted request, including real name
        // of the driver
        return rideOffers.stream()
                .filter(ride -> ride.getRequests().stream().anyMatch(req -> req.getCompanyUserId() != null &&
                        req.getCompanyUserId().equals(companyUserId) &&
                        req.getStatus() == RideRequest.Status.ACCEPTED))
                .toList();
    }
}