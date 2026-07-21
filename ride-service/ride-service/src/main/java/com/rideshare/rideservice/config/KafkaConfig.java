package com.rideshare.rideservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaConfig{

    //Topic where Ride Service published ride request
    //Matching Sercice subscribers to this topic
    @Bean
    public NewTopic rideRequestedTopic(){
        return TopicBuilder.name("ride.requested")
        .partitions(3)
        .replicas(1)
        .build();
    }

    //Topicc where Matching service published match results
    //Ride service subscribes to this topic
    @Bean
    public NewTopic rideMatchedTopic(){
        return TopicBuilder.name("ride.matched")
        .partitions(3)
        .replicas(1)
        .build();
    }
}