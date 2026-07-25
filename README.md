# Microservice-Based Ride Dispatch System 🚙

## Overview:
It's a microservice-based ride dispatch system that manages ride requests, live driver locations, automatic driver matching and complete ride sharing lifecycle. More other microservices will be added to the system soon...
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

## Details

1. Location Service

- Get the driver locations every 3s
- Get the nearby drivers
- Handle the drivers go off-line, remove

2. Ride Service
- Get the request for ride, store the ride into database
- Search the ride by ID
- Search ride by riderID
- Update the ride status
  1. start
  2. complete
  3. cancel

3. Matching Service
- Have nearby driver response
  1. Ask Location service to search nearby drivers
  2. Score each driver and pick the top N
  3. Publish the ride match event to Kafka
