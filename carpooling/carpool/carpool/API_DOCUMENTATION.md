# Carpooling System API Documentation

## Base URL
`/rides`

---

## Ride Offer Endpoints

### Create a Ride Offer
- **POST** `/rides/create`
- **Request Body:**
```json
{
  "startPoint": "Delhi",
  "stops": ["Connaught Place", "Akshardham"],
  "destination": "Noida",
  "dateTime": "2024-06-10T09:00:00",
  "availableSeats": 3,
  "companyUserId": "EMP12345"
}
```
- **Response:** RideOffer object

---

### List All Available Rides
- **GET** `/rides/all`
- **Returns:** List of rides with available seats and future date/time only.

---

### Search Rides (Advanced Filter)
- **GET** `/rides/search?startPoint=...&destination=...&date=YYYY-MM-DD&stop=...&minSeats=...`
- **All parameters optional.**
- **Returns:** List of rides matching filters, only with available seats and future date/time.

---

### Edit a Ride Offer
- **PUT** `/rides/{id}`
- **Request Body:** Same as create
- **Response:** Updated RideOffer object

---

### Delete a Ride Offer
- **DELETE** `/rides/{id}`
- **Response:** None

---

## Ride Request Endpoints

### Request to Join a Ride
- **POST** `/rides/{rideId}/requests`
- **Request Body:**
```json
{
  "companyUserId": "EMP67890"
}
```
- **Response:** RideRequest object
- **Edge Cases:**
  - Cannot request own ride
  - Cannot request same ride twice

---

### List Requests for a Ride
- **GET** `/rides/{rideId}/requests`
- **Returns:** List of requests for the ride

---

### Accept a Ride Request
- **POST** `/rides/{rideId}/requests/{requestId}/accept`
- **Response:** Updated RideRequest object
- **Edge Cases:**
  - No overbooking (seats cannot go below zero)

---

### Reject a Ride Request
- **POST** `/rides/{rideId}/requests/{requestId}/reject`
- **Response:** Updated RideRequest object

---

## Data Models

### RideOffer
```json
{
  "id": 1,
  "startPoint": "Delhi",
  "stops": ["Connaught Place", "Akshardham"],
  "destination": "Noida",
  "dateTime": "2024-06-10T09:00:00",
  "availableSeats": 3,
  "companyUserId": "EMP12345",
  "requests": [ ... ]
}
```

### RideRequest
```json
{
  "id": 1,
  "rideId": 1,
  "status": "PENDING", // or "ACCEPTED", "REJECTED"
  "companyUserId": "EMP67890"
}
```

---

## Edge Case Protections
- Cannot request own ride
- Cannot request same ride twice
- No overbooking (seats cannot go below zero)
- Expired rides are hidden from all/search endpoints
- All actions require a valid `companyUserId`

---

## Notes
- All date/time fields use ISO 8601 format (e.g., `2024-06-10T09:00:00`)
- All endpoints return JSON
- No user-identifying information is exposed in API responses
- For company verification, always provide `companyUserId` in requests 

---

## Monitoring & Management: Spring Boot Actuator

Spring Boot Actuator is integrated for real-time monitoring and management of the backend service.

### Common Actuator Endpoints
- **Health:** `/actuator/health` — Application health status
- **Metrics:** `/actuator/metrics` — JVM, system, and custom metrics
- **Info:** `/actuator/info` — Application info (customizable)
- **Beans:** `/actuator/beans` — List of Spring beans
- **Environment:** `/actuator/env` — Environment properties
- **Loggers:** `/actuator/loggers` — Logger levels
- **Mappings:** `/actuator/mappings` — All request mappings

All endpoints are available at `/actuator/*` (see `application.properties`).

### Example Usage
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)

### Security Note
- **For production, restrict access to actuator endpoints!**
- Use Spring Security or expose only selected endpoints as needed.

### Custom Info
You can add custom info in `application.properties`:
```properties
info.app.name=Carpooling System
info.app.version=1.0.0
```
Then check `/actuator/info`.

--- 

---

## User Ride History

The system provides endpoints for users to view their ride history, including real names for privacy within their own history only.

### Offered Rides History
- **GET** `/history/offered?companyUserId=...`
- **Description:** Returns all rides offered by the user, including the real names of accepted passengers.
- **Response Example:**
```json
[
  {
    "id": 1,
    "startPoint": "Delhi",
    "destination": "Noida",
    "dateTime": "2024-06-10T09:00:00",
    "realName": "Driver Real Name",
    "requests": [
      { "companyUserId": "EMP2002", "realName": "Passenger Real Name", "status": "ACCEPTED" }
    ]
  }
]
```

### Taken Rides History
- **GET** `/history/taken?companyUserId=...`
- **Description:** Returns all rides the user has joined as a passenger, including the real name of the driver.
- **Response Example:**
```json
[
  {
    "id": 2,
    "startPoint": "Delhi",
    "destination": "Noida",
    "dateTime": "2024-06-12T09:00:00",
    "realName": "Driver Real Name",
    "requests": [
      { "companyUserId": "EMP1001", "realName": "Passenger Real Name", "status": "ACCEPTED" }
    ]
  }
]
```

- **Privacy Note:** Real names are only visible to the user in their own history endpoints and are not exposed in public ride or request APIs.

--- 