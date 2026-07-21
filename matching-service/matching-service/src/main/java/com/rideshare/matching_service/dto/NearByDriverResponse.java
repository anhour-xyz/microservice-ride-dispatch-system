package com.rideshare.matching_service.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
//Response recieved from locaiton service
//When quering for nearby drivers
public class NearByDriverResponse{
    private String driverId;
    private double latitude;
    private double longitude;
    private double distanceInKm;
}