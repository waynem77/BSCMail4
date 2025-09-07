# BSCMail4

A Spring Boot application for volunteer management.

## Prerequisites

- Java 17 or higher
- PostgreSQL database server
- Gradle (or use the included Gradle wrapper)

## Running the Application

This application uses Spring profiles to manage different database connections. You **must** specify a profile to run the application.

### Available Profiles

- **`production`**: Connects to PostgreSQL using `bscmail-prod` user
- **`developer`**: Connects to PostgreSQL using `bscmail-dev` user with enhanced logging
- **`generatesql`**: Used for generating SQL schema files

### Running with Production Profile

```bash
./gradlew bootRun -Dspring.profiles.active=production
```

### Running with Developer Profile

```bash
./gradlew bootRun -Dspring.profiles.active=developer
```

### Generating SQL Schema

To generate SQL schema files:

```bash
./gradlew generateSql
```
