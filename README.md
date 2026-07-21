# Microservice-Based Ride Dispatch System 🚙

## Overview:
It's a microservice-based ride dispatch system that manages ride requests, live driver locations, automatic driver matching and complete ride sharing lifecycle. More microservices will be added to the system.
<br>

<ins> Tech Stack: Java, Spring Boot, Redis, Kafka, MySQL, Docker, AWS </ins>

<br>

## Architecture
```text
Driver Phone ──► Location Service ──► Redis (GEOADD)

Rider App ─────► Ride Service ───────► Kafka (ride.requested)
                                        │
                                        ▼
                             Matching Service (Consumer)
                                        │
                                        ▼
                        Location Service (find nearby drivers)
                                        │
                                        ▼
                        Matching Algorithm (score drivers)
                                        │
                                        ▼
                              Kafka (ride.matched)
                                        │
                                        ▼
                    Ride Service (update ride with driver)
```

##
