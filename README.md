# Microservice-Based Ride Dispatch System 🚙

[Demo](https://relay-ride-dispatch.vercel.app/)

## Overview:
This is a microservice-based ride dispatch system that manages ride requests, live driver locations, automatic driver matching and complete ride sharing lifecycle. The main purpose is to build a scalable and low-latency backend system that can efficiently discover available drivers and make optimal matching decisions. It aims to provide a reliable using experience for both customers and drivers.

Update: User service, authentication service and notification service are addded for system authentication and authorization
<br>

<ins> Tech Stack: Java, Spring Boot, Redis, Kafka, MySQL, Docker, AWS </ins>


<br>

## Architecture
```text
                         Rider App / Driver App
                                  |
                                  |
                            API Gateway
                    (Routing + JWT Validation)
                                  |
          ------------------------------------------------
          |              |              |                |
 Authentication     User Service    Ride Service   Location Service
    Service             |              |                |
          |             |              |                |
          |             |              |                |
   MySQL (Auth DB)  MySQL(User DB) MySQL(Ride DB)  Redis GEO
          |
          |
          |

                       Kafka
          --------------------------------
          |              |               |

 Matching Service  Notification Service  Analytics Service
       |                  |
       |                  |

 Location Service    Firebase Cloud Messaging
       |
       |

 Redis GEO Search
 (Nearby Drivers)
```

## Project Details

1. Location Service

- Real-time location tracking
- Redis Geospatial
- Location update APIs
- Driver availability zones

<br>

2. Ride Service
- Get the request for ride, store the ride into database
- Search the ride by ID
- Search ride by riderID
- Update the ride status
  1. start
  2. complete
  3. cancel

<br>

3. Matching Service
- Have nearby driver response
  1. Ask Location service to search nearby drivers
  2. Score each driver and pick the top N
  3. Publish the ride match event to Kafka

<br>
 
4. Notification Service
- Firebase Cloud Messaging
- Driver notification (new ride request)
- Rider notification (driver accepted)
- Real-time updates
- Email notifications

<br>

6. User Service
- User Registration
- Driver Registration
- Account Management

<br>

7. Authentication Service
- Phone number authentication
- OTP verification
- JWT tokens

<br>

