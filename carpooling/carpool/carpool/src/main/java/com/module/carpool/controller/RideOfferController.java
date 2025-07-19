package com.module.carpool.controller;

import com.module.carpool.model.RideOffer;
import com.module.carpool.model.RideRequest;
import com.module.carpool.service.RideOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rides")
public class RideOfferController {
    private final RideOfferService rideOfferService;

    @Autowired
    public RideOfferController(RideOfferService rideOfferService) {
        this.rideOfferService = rideOfferService;
    }

    @PostMapping("/create")
    public RideOffer createRide(@RequestBody RideOffer rideOffer) {
        return rideOfferService.createRide(rideOffer);
    }

    @GetMapping("/all")
    public List<RideOffer> getAllRides() {
        return rideOfferService.getAllRides();
    }

    @GetMapping("/search")
    public List<RideOffer> searchRides(
            @RequestParam(required = false) String startPoint,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date, // format: yyyy-MM-dd
            @RequestParam(required = false) String stop,
            @RequestParam(required = false) Integer minSeats) {
        return rideOfferService.searchRides(startPoint, destination, date, stop, minSeats);
    }

    @DeleteMapping("/{id}")
    public void deleteRide(@PathVariable Long id) {
        rideOfferService.deleteRide(id);
    }

    @PutMapping("/{id}")
    public RideOffer updateRide(@PathVariable Long id, @RequestBody RideOffer updatedRide) {
        return rideOfferService.updateRide(id, updatedRide);
    }

    @PostMapping("/{rideId}/requests")
    public RideRequest requestToJoin(@PathVariable Long rideId, @RequestBody RideRequest request) {
        return rideOfferService.requestToJoin(rideId, request);
    }

    @GetMapping("/{rideId}/requests")
    public List<RideRequest> getRequestsForRide(@PathVariable Long rideId) {
        return rideOfferService.getRequestsForRide(rideId);
    }

    @PostMapping("/{rideId}/requests/{requestId}/accept")
    public RideRequest acceptRequest(@PathVariable Long rideId, @PathVariable Long requestId) {
        return rideOfferService.acceptRequest(rideId, requestId);
    }

    @PostMapping("/{rideId}/requests/{requestId}/reject")
    public RideRequest rejectRequest(@PathVariable Long rideId, @PathVariable Long requestId) {
        return rideOfferService.rejectRequest(rideId, requestId);
    }

    @GetMapping("/history/offered")
    public List<RideOffer> getOfferedHistory(@RequestParam String companyUserId) {
        return rideOfferService.getOfferedHistory(companyUserId);
    }

    @GetMapping("/history/taken")
    public List<RideOffer> getTakenHistory(@RequestParam String companyUserId) {
        return rideOfferService.getTakenHistory(companyUserId);
    }
}