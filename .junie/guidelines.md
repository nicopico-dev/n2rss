# N2RSS Project Development Guidelines

This document provides essential information for developers working on the N2RSS project.

## Build and Configuration Instructions

### Prerequisites

- Java 17 or higher
- Gradle (wrapper included)
- MariaDB for production, H2 for tests

### Building the Project

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create deployable JAR
./gradlew bootJar

# Deploy to the deploy directory
./gradlew copyJarToDeploy
```

### Configuration

The application uses Spring Boot's configuration system with the following profiles:

- `local`: Default development profile
- `reset-db`: Resets the database on startup
- `test`: Used for testing with H2 database

Key configuration files:

- `src/main/resources/application.properties`: Main configuration
- `src/test/resources/application-test.properties`: Test-specific configuration

Environment variables required for production:

- `N2RSS_EMAIL_HOST`: Email server host
- `N2RSS_EMAIL_PORT`: Email server port (default: 993)
- `N2RSS_EMAIL_USERNAME`: Email username
- `N2RSS_EMAIL_PASSWORD`: Email password
- `N2RSS_EMAIL_INBOX_FOLDERS`: Email inbox folders (default: inbox)
- `N2RSS_RECAPTCHA_SITE_KEY`: reCAPTCHA site key (if enabled)
- `N2RSS_RECAPTCHA_SECRET_KEY`: reCAPTCHA secret key (if enabled)
- `N2RSS_GITHUB_ACCESS_TOKEN`: GitHub access token (if monitoring enabled)

## Testing Information

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "fr.nicopico.n2rss.utils.ListExtKtTest"

# Run a specific test method
./gradlew test --tests "fr.nicopico.n2rss.utils.ListExtKtTest.sortBy should sort items by specified property in ascending order"
```

### Test Structure

Tests follow a consistent structure:

- JUnit 5 for test framework
- MockK for mocking (not Mockito)
- Kotest for assertions
- GreenMail for email testing
- MockWebServer for HTTP testing

### Writing Tests

1. Create a test class in the same package as the class being tested, with the suffix `Test` or `KtTest` for extension
   functions
2. Use descriptive test names with backticks: `` `method should do something when condition` ``
3. Follow the GIVEN-WHEN-THEN pattern in test methods
4. Use Kotest assertions with the `shouldBe` infix function
5. For mocking, use MockK annotations and verification

Example test:

```kotlin
@Test
fun `sortBy should sort items by specified property in ascending order`() {
    // GIVEN
    val items = listOf(
        TestItem("Charlie", 30),
        TestItem("Alice", 25),
        TestItem("Bob", 35)
    )
    val sort = Sort.by(Sort.Direction.ASC, "name")

    // WHEN
    val result = items.sortBy(sort) { item, prop ->
        when (prop) {
            "name" -> item.name
            "age" -> item.age
            else -> null
        }
    }

    // THEN
    result shouldBe listOf(
        TestItem("Alice", 25),
        TestItem("Bob", 35),
        TestItem("Charlie", 30)
    )
}
```

### Code Coverage Requirements

The project requires a minimum of 80% code coverage. Certain classes are excluded from coverage requirements:

- Application entry point classes
- Configuration classes
- ConfigurationProperties classes

## Development Information

### Code Style

- Kotlin code follows the official Kotlin style guide
- Uses strict null safety with JSR-305 annotations (`-Xjsr305=strict`)
- Uses Detekt for static code analysis

### Project Structure

- Spring Boot application with Kotlin
- Uses Spring Data JPA for database access
- Uses Flyway for database migrations
- Separate migration paths for MariaDB (production) and H2 (tests)

### Adding or updating support for a newsletter

Follow the directives in [NewsletterHandler Guidelines](./newsletterhandler-guidelines.md)

### Custom Gradle Plugins

The project uses several custom Gradle plugins:

- `kotlin-strict`: Enforces strict Kotlin compiler settings
- `quality`: Configures code coverage requirements
- `deploy`: Sets up deployment tasks
- `restartServerTest`: Configures server restart testing

### Database

- MariaDB for production
- H2 for tests
- Flyway for migrations with separate paths for MariaDB and H2

### Deployment

The project includes a custom deployment task:

```bash
./gradlew copyJarToDeploy
```

This creates a JAR file named `n2rss.jar` in the `deploy` directory.

### Continuous Integration

The project includes test scripts for CI/CD:

- `test_ci.sh`: CI test script
- `test_cd.sh`: CD test script
- `test_restart_server.sh`: Server restart test script
