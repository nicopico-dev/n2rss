# Build and Configuration

## Prerequisites

- Java 17 or higher
- Gradle (wrapper included)
- MariaDB for production, H2 for tests

## Build Commands

```bash
# Build the project
./gradlew build

# Create deployable JAR
./gradlew bootJar
```

## Configuration

The application uses Spring Boot's configuration system with the following profiles:

- `local`: Default development profile
- `reset-db`: Resets the database on startup
- `test`: Used for testing with H2 database

### Configuration Files

- `src/main/resources/application.properties`: Main configuration
- `src/test/resources/application-test.properties`: Test-specific configuration

### Environment Variables

Required for production:

- `N2RSS_EMAIL_HOST`: Email server host
- `N2RSS_EMAIL_PORT`: Email server port (default: 993)
- `N2RSS_EMAIL_USERNAME`: Email username
- `N2RSS_EMAIL_PASSWORD`: Email password
- `N2RSS_EMAIL_INBOX_FOLDERS`: Email inbox folders (default: inbox)
- `N2RSS_RECAPTCHA_SITE_KEY`: reCAPTCHA site key (if enabled)
- `N2RSS_RECAPTCHA_SECRET_KEY`: reCAPTCHA secret key (if enabled)
- `N2RSS_GITHUB_ACCESS_TOKEN`: GitHub access token (if monitoring enabled)

## Database

- MariaDB for production
- H2 for tests
- Flyway for migrations with separate paths for MariaDB and H2