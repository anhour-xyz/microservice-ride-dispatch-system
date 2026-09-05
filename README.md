# Microservice-Based Ride Dispatch System 🚙

## Overview:
This is a microservice-based ride dispatch backend system that manages ride requests, live driver locations, automatic driver matching and complete ride sharing lifecycle. User service, authentication service and notification service are also integrated into the system for user authentication and authorization. The main purpose is to build a scalable and low-latency backend system that can efficiently discover available drivers and make optimal matching decisions. It aims to provide a reliable using experience for both customers and drivers.

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

- Real-time location tracking based on Redis Geospatial data
- Location update APIs
- Driver availability zones

<br>

<img width="560" height="290" alt="image" src="https://github.com/user-attachments/assets/97951607-75ff-4a98-a1a3-14874255f9f2" />

<br>
<br>

2. Ride Service
- Get the request for ride, store the ride into MySQL database
- Search the ride by ID, Search ride by riderID
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

<img width="560" height="290" alt="image" src="https://github.com/user-attachments/assets/87d4c5ba-713b-4329-b2fd-408d2f8df4d4" />

<br>
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
- Email authentication
- OTP verification
- JWT tokens

<br>

<img width="560" height="290" alt="image" src="https://github.com/user-attachments/assets/b1f784b3-de80-4f42-8027-b1e84767de6b" />


## APIs Development

```text

Location Service:

POST /api/v1/locationsdrivers/update
GET /api/v1/locations/drivers/nearby
DELETE /api/v1/locations/drivers/{driverId}



Ride Service:

POST /api/v1/rides/request
GET /api/v1/rides/{riderId}
GET /api/v1/rides/rider/{rideId}
PUT /api/v1/rides/{rideId}/start
PUT /api/v1/rides/{rideId}/complete
PATCH /api/v1/rides/{rideId}/cancel



User Service:

POST /api/v1/users/me
GET /api/v1/users/me
PATCH /api/v1/users/me
DELETE /api/v1/users/me



Authentication Service:

POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET /api/v1/auth/me
POST /api/v1/auth/verify-email
POST /api/v1/auth/resend-verification

```


