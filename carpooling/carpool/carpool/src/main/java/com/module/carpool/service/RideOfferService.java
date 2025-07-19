package com.module.carpool.service;

import com.module.carpool.model.RideOffer;
import com.module.carpool.model.RideRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RideOfferService {
    private final List<RideOffer> rideOffers = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final AtomicLong requestIdGenerator = new AtomicLong();

    public RideOffer createRide(RideOffer rideOffer) {
        rideOffer.setId(idGenerator.incrementAndGet());
        rideOffers.add(rideOffer);
        return rideOffer;
    }

    public List<RideOffer> getAllRides() {
        LocalDateTime now = LocalDateTime.now();
        return rideOffers.stream()
                .filter(ride -> ride.getAvailableSeats() > 0)
                .filter(ride -> ride.getDateTime().isAfter(now))
                .toList();
    }

    public List<RideOffer> searchRides(String startPoint, String destination, String date, String stop,
            Integer minSeats) {
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

    public void deleteRide(Long id) {
        rideOffers.removeIf(ride -> ride.getId().equals(id));
    }

    public RideOffer updateRide(Long id, RideOffer updatedRide) {
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

    public RideRequest requestToJoin(Long rideId, RideRequest request) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        RideOffer ride = rideOpt.get();
        if (ride.getCompanyUserId() != null && ride.getCompanyUserId().equals(request.getCompanyUserId())) {
            throw new RuntimeException("You cannot request to join your own ride.");
        }
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

    public List<RideRequest> getRequestsForRide(Long rideId) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        return rideOpt.get().getRequests();
    }

    public RideRequest acceptRequest(Long rideId, Long requestId) {
        Optional<RideOffer> rideOpt = rideOffers.stream().filter(r -> r.getId().equals(rideId)).findFirst();
        if (rideOpt.isEmpty())
            throw new RuntimeException("Ride not found");
        RideOffer ride = rideOpt.get();
        for (RideRequest req : ride.getRequests()) {
            if (req.getId().equals(requestId)) {
                if (ride.getAvailableSeats() <= 0)
                    throw new RuntimeException("No seats available");
                req.setStatus(RideRequest.Status.ACCEPTED);
                ride.setAvailableSeats(ride.getAvailableSeats() - 1);
                return req;
            }
        }
        throw new RuntimeException("Request not found");
    }

    public RideRequest rejectRequest(Long rideId, Long requestId) {
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

    public List<RideOffer> getOfferedHistory(String companyUserId) {
        return rideOffers.stream()
                .filter(ride -> ride.getCompanyUserId() != null && ride.getCompanyUserId().equals(companyUserId))
                .toList();
    }

    public List<RideOffer> getTakenHistory(String companyUserId) {
        return rideOffers.stream()
                .filter(ride -> ride.getRequests().stream().anyMatch(req -> req.getCompanyUserId() != null &&
                        req.getCompanyUserId().equals(companyUserId) &&
                        req.getStatus() == RideRequest.Status.ACCEPTED))
                .toList();
    }
}