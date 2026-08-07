package com.rideshare.rideservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class kafka {
    
    @Bean
    public NewTopic rideRequestedTopic(){
        return TopicBuilder.name("ride.requested")
        .partitions(3)
        .replicas(1)
        .build();
    }

    @Bean
    public NewTopic rideMatchedTopic(){
        return TopicBuilder.name("ride.matched")
        .partitions(3)
        .replicas(1)
        .build();
    }
}
