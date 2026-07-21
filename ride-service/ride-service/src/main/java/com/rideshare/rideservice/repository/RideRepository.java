package com.rideshare.rideservice.repository;

import java.util.List;

import com.rideshare.rideservice.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, String>{
    
    List<Ride> findByRiderIdOrderByCreatedAtDesc(String riderId);
}
