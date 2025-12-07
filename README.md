# BSCMail4

A Spring Boot application with separate server and client services that communicate via REST API.

## Architecture

This project consists of two services that run on separate ports but are packaged in a single JAR:

- **Server Service** (Port 8080): REST API providing business logic
- **Client Service** (Port 8081): Thymeleaf-based web interface

The client communicates with the server via REST API calls.

## Project Structure

```
src/main/java/io/github/waynem77/bscmail4/
├── server/              # Server service package
│   ├── ServerApplication.java
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic services
│   └── dto/              # Data Transfer Objects
├── client/               # Client service package
│   ├── ClientApplication.java
│   ├── controller/       # Web controllers (Thymeleaf)
│   └── dto/              # Data Transfer Objects
└── Launcher.java       # Main launcher to start both services

src/main/resources/
├── application-server.properties   # Server configuration
├── application-client.properties  # Client configuration
└── templates/            # Thymeleaf templates
```

## Building the Project

```bash
./gradlew build
```

This will create a JAR file in `build/libs/BSCMail4-4.0-SNAPSHOT.jar`

## Running the Application

### Option 1: Run Both Services Together (Recommended)

Use the launcher to start both services from a single JAR:

```bash
java -jar build/libs/BSCMail4-4.0-SNAPSHOT.jar
```

This will start:
- Server service at http://localhost:8080
- Client service at http://localhost:8081

### Option 2: Run Services Separately

You can also run each service independently:

**Server only:**
```bash
./gradlew bootRun --args='--spring.profiles.active=server'
```

Or:
```bash
java -jar build/libs/BSCMail4-4.0-SNAPSHOT.jar --spring.profiles.active=server
```

**Client only:**
```bash
./gradlew bootRun --args='--spring.profiles.active=client'
```

Or:
```bash
java -jar build/libs/BSCMail4-4.0-SNAPSHOT.jar --spring.profiles.active=client
```

## API Endpoints

### Server REST API (Port 8080)

- `GET /api/health` - Health check endpoint
- `GET /api/messages` - Get all messages
- `GET /api/messages/{id}` - Get message by ID
- `POST /api/messages` - Create a new message

### Client Web Interface (Port 8081)

- `GET /` - List all messages
- `GET /messages/{id}` - View message details
- `GET /messages/new` - Create new message form
- `POST /messages` - Submit new message

## Development

### Prerequisites

- Java 17 or higher
- Gradle 7.x or higher

### Running in Development Mode

For development, you can run the services separately using Gradle:

```bash
# Terminal 1 - Server
./gradlew bootRun --args='--spring.profiles.active=server'

# Terminal 2 - Client
./gradlew bootRun --args='--spring.profiles.active=client'
```

## Configuration

### Server Configuration (`application-server.properties`)

- Port: 8080
- Thymeleaf: Disabled (REST API only)

### Client Configuration (`application-client.properties`)

- Port: 8081
- Thymeleaf: Enabled
- Server API URL: http://localhost:8080/api

## Features

- ✅ Separate server and client services
- ✅ REST API communication between services
- ✅ Thymeleaf templates for web interface
- ✅ Single JAR deployment
- ✅ Component isolation via package scanning
- ✅ Sample CRUD operations for messages

## Next Steps

1. Add database persistence (JPA/Hibernate)
2. Add authentication and authorization
3. Add input validation and error handling
4. Add unit and integration tests
5. Add logging and monitoring
6. Add API documentation (Swagger/OpenAPI)