# Carpooling Backend System

A Spring Boot backend for an organizational carpooling system. This project allows users to offer rides, request to join rides, manage ride requests, and view ride history, with privacy and company verification features.

---

## Features
- Offer a ride with multiple stops and destination
- Advanced ride search and filtering
- Request to join rides (with company verification)
- Accept/reject ride requests
- Automatic seat management and ride visibility
- Anonymous public endpoints, with real names only in user history
- User ride history (offered and taken)
- Edge case protections (no duplicate requests, no overbooking, etc.)
- Monitoring and management with Spring Boot Actuator
- Comprehensive API documentation
- Ready for integration with React Native or other frontends

---

## Getting Started

### Prerequisites
- Java 17+
- Maven
- (Optional) GitHub CLI for repo management

### Setup
1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   cd carpooling/carpool/carpool
   ```
2. **Build the project:**
   ```sh
   ./mvnw clean install
   ```
3. **Run the application:**
   ```sh
   ./mvnw spring-boot:run
   ```
   The backend will start on [http://localhost:8080](http://localhost:8080) by default.

---

## API Documentation

See [API Documentation](carpool/carpool/API_DOCUMENTATION.md) for full details on all endpoints, request/response formats, edge cases, and monitoring features.

---

## Configuration

Key settings in `src/main/resources/application.properties`:
- `server.port` — Change the server port
- Logging levels and log file output
- Actuator endpoint exposure and health details

---

## Testing

Run all tests with:
```sh
./mvnw test
```
See `src/test/java/com/module/carpool/RideOfferControllerTest.java` for comprehensive test coverage.

---

## Contribution

1. Fork the repo and create your branch: `git checkout -b feature/your-feature`
2. Commit your changes: `git commit -am 'Add new feature'`
3. Push to the branch: `git push origin feature/your-feature`
4. Open a Pull Request

---

## License

This project is for organizational/internal use. For external use, please contact the maintainers.

---

## Contact
For questions or support, please contact the backend team or open an issue in the repository.
