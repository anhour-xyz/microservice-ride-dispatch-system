package com.rideshare.rideservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.event.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    public RideResponse requestRide(RideRequest request) {
        log.info("New ride request from rider: {}", request.getRiderId());

        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimatedFare(request));

        Ride savedRide = rideRepository.save(ride);
        RideRequestedEvent event = new RideRequestedEvent(
            savedRide.getId(),
            savedRide.getRiderId(),
            savedRide.getPickupLatitude(),
            savedRide.getPickupLongitude(),
            savedRide.getPickupAddress(),
            savedRide.getDropLatitude(),
            savedRide.getDropLongitude(),
            savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published for ride: {}", savedRide.getId());

        savedRide.setStatus(RideStatus.MATCHING);
        return mapToResponse(rideRepository.save(savedRide));
    }

    public void updateRideWithDriver(String rideId, String driverId) {
        Ride ride = findRide(rideId);
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    public RideResponse startRide(String rideId) {
        Ride ride = findRide(rideId);
        requireStatus(ride, RideStatus.ACCEPTED, "started");
        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        return mapToResponse(rideRepository.save(ride));
    }

    public RideResponse cancelRide(String rideId) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalStateException(
                "Ride can't be cancelled. Current status: " + ride.getStatus());
        }
        ride.setStatus(RideStatus.CANCELLED);
        return mapToResponse(rideRepository.save(ride));
    }

    public RideResponse completeRide(String rideId) {
        Ride ride = findRide(rideId);
        requireStatus(ride, RideStatus.RIDE_STARTED, "completed");
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        return mapToResponse(rideRepository.save(ride));
    }

    public RideResponse getRideById(String rideId) {
        return mapToResponse(findRide(rideId));
    }

    public List<RideResponse> getRidesByRider(String riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private Ride findRide(String rideId) {
        return rideRepository.findById(rideId)
            .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + rideId));
    }

    private void requireStatus(Ride ride, RideStatus expectedStatus, String action) {
        if (ride.getStatus() != expectedStatus) {
            throw new IllegalStateException(
                "Ride can't be " + action + ". Current status: " + ride.getStatus());
        }
    }

    private double calculateEstimatedFare(RideRequest request) {
        double pickupLatitude = Math.toRadians(request.getPickupLatitude());
        double dropLatitude = Math.toRadians(request.getDropLatitude());
        double latitudeDifference = dropLatitude - pickupLatitude;
        double longitudeDifference = Math.toRadians(
            request.getDropLongitude() - request.getPickupLongitude());

        double haversine = Math.pow(Math.sin(latitudeDifference / 2), 2)
            + Math.cos(pickupLatitude) * Math.cos(dropLatitude)
            * Math.pow(Math.sin(longitudeDifference / 2), 2);
        double distanceKm = 6371 * 2 * Math.asin(Math.sqrt(haversine));
        double fare = 50 + (distanceKm * 12);
        return Math.round(fare * 100.0) / 100.0;
    }

    private RideResponse mapToResponse(Ride ride) {
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setUpdatedAt(ride.getUpdatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }
}
