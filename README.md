# Microservice-Based Ride Dispatch System 🚙

## Overview:
A web-based platform enabling users to create events, manage ticket sales and generate QR coded tickets for attendees, streamlining the event management and ticket distribution process
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
