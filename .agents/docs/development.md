# Development

## Code Style

- Kotlin code follows the official Kotlin style guide
- Uses strict null safety with JSR-305 annotations (`-Xjsr305=strict`)
- Uses Detekt for static code analysis

## Project Structure

- Spring Boot application with Kotlin
- Uses Spring Data JPA for database access
- Uses Flyway for database migrations

## Custom Gradle Plugins

The project uses several custom Gradle plugins:

- `kotlin-strict`: Enforces strict Kotlin compiler settings
- `quality`: Configures code coverage requirements
- `deploy`: Sets up deployment tasks
- `restartServerTest`: Configures server restart testing