package com.rideshare.matching_service.client;
import org.springframework.cloud.openfeign.FeignClient;
import com.rideshare.matching_service.dto.NearByDriverResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "location-service", url = "${location.service.url}")
public interface LocationServiceClient {
    
    @GetMapping("/api/v1/locations/drivers/nearby")
    List<NearByDriverResponse> getNearByDrivers(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam double radius
    );
}
