# N2RSS Project Guidelines

## Project Overview

N2RSS (Newsletter to RSS) is a service that extracts articles from newsletters and publishes them as RSS feeds. The
project aims to make newsletter content more accessible by converting it into a format that can be consumed by RSS
readers.

### Core Functionality

- Periodically checks an email inbox for new newsletters
- Processes recognized newsletters and extracts individual articles
- Publishes each newsletter as a separate RSS feed
- Maps each article from a newsletter to an individual RSS entry
- Marks processed emails as read

### Supported Newsletters

The application currently supports the following newsletters:

- Android Weekly
- Built for Mars
- Kotlin Weekly
- MIT - The Download
- MIT - Weekend Reads
- Pointer
- QuickBird

## Technical Architecture

### Technology Stack

- **Programming Languages**: Kotlin 2.1, Java 17
- **Frameworks**: Spring MVC, Spring Data, Jakarta EE
- **Libraries**:
    - JSOUP for parsing email content
    - ROME for generating RSS feeds
- **Database**: MariaDB
- **Development Tools**: Docker (for development environment)

### Key Components

1. **Email Client**: Connects to an email server to retrieve newsletters
    - Can be configured with environment variables
    - Supports a local development mode using stub files

2. **Newsletter Handlers**: Process specific newsletter formats
    - Each handler is responsible for a specific newsletter type
    - Extracts articles and metadata from the newsletter

3. **RSS Feed Generator**: Creates and serves RSS feeds
    - Generates XML in standard RSS format
    - Provides endpoints for accessing feeds

4. **Analytics**: Tracks usage through Simple Analytics
    - GDPR-compliant
    - Primarily server-side events
    - No user data collection

5. **Monitoring**: Reports errors through GitHub issues
    - Creates issues with the label "n2rss-bot"

## Development Guidelines

### Local Development

1. Use the `local` profile for faster development:
   ```shell
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

2. The local profile uses stub emails from `stubs/emails` instead of connecting to an actual email server

3. A Docker container for MariaDB is automatically created for development

### Testing

- Run tests with `./gradlew check`
- Newsletter handlers are tested with emails stored in `src/main/resources/emails`
- Minimum code coverage requirement: 80%

### Deployment

The application requires:

- Access to an email account
- A MariaDB database

Deployment steps and required environment variables are detailed in the README.md file.

## API Endpoints

- `GET /`: HTML presentation of the project
- `GET /rss`: Information on available RSS feeds
- `GET /rss/[code]`: Retrieve a specific RSS feed
- `POST /notifyRelease?version=X.Y.Z`: Notify analytics of a new release
- `POST /stop`: Stop the application (protected by secret key)

## License

This project is licensed under the MIT License.
