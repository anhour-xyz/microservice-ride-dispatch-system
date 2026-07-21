package com.rideshare.matching_service.event;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

//Event consumed from Kafka topic: ride.requested
//Published by rider service when a rider request a ride

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestedEvent {
    
    private String riderId;
    private String rideId;
    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
}
