# N2RSS

N2RSS (*Newsletter to RSS*) goal is to publish RSS feeds containing separate articles
extracted from selected newsletters.

It will run an email check periodically for new emails on an inbox.
A recognized email will be processed, while an unrecognized email is left as-is.
Once an email has been processed, it is marked as read.

Each newsletter will map to a separate RSS feed,
and each article extracted from the newsletter publication will map to different RSS entry in the corresponding feed.

| Newsletter          | URL                                                                  |
|---------------------|----------------------------------------------------------------------|
| Android Weekly      | https://androidweekly.net                                            |
| Built for Mars      | https://builtformars.com                                             |
| Kotlin Weekly       | https://kotlinweekly.net                                             |
| MIT - The Download  | https://forms.technologyreview.com/newsletters/briefing-the-download |
| MIT - Weekend Reads | https://forms.technologyreview.com/newsletters/tech-weekend-reads/   |
| Pointer             | https://www.pointer.io                                               |
| QuickBird           | https://quickbirdstudios.com/blog                                    |

## Built With

- Kotlin 2.1
- Java 17
- Jakarta EE
- Spring MVC
- Spring Data
- JSOUP to parse email content
- ROME to generate RSS feeds

## Getting Started

These instructions will get you a copy of the project 
up and running on your local machine for development 
and testing purposes.

### Prerequisites

- Java SDK version 17
- Kotlin API version 2.1
- IDE that supports Jakarta EE, Spring, Java and Kotlin development (For example, IntelliJ IDEA)
- Docker (for development purposes only)

### Installing

This project is a Spring Boot application written in Kotlin

By default, it will connect an `EmailClient` using parameters provided as environment variables:
```
N2RSS_EMAIL_HOST=<host url of the email account>
N2RSS_EMAIL_PORT=993
N2RSS_EMAIL_PROTOCOL=imaps
N2RSS_EMAIL_USERNAME=<username for the email account>
N2RSS_EMAIL_PASSWORD=<password for the email account>
```

By using the `local` profile, a `ResourceFileEmailClient` will be used instead and the files located 
at `stubs/emails` will be the inbox. This profile is recommended to get a faster feedback-loop while developing.

```shell
$ ./gradlew bootRun --args='--spring.profiles.active=local'
```

When run for development through an IDE or with `bootRun`, Spring Boot will use a Docker container 
to run the **MariaDB database**. This Docker container will be created automatically on the first run, 
using the declarations in `compose.yaml`.

## Usage

Once running, this application provides the following endpoints

### GET /
Get a presentation of the project as an HTML page. 
The information provided by this page is the same as [GET /rss](#get-rss)

### GET /rss
Get information on the RSS feeds handled by the application.

Example:
```
GET http://localhost/rss

HTTP/1.1 200 OK
Connection: keep-alive
content-type: application/json
date: Sun, 03 Mar 2024 21:27:01 GMT
via: 1.1 alproxy
transfer-encoding: chunked

[
  {
    "code": "android_weekly",
    "title": "Android Weekly",
    "publicationCount": 3,
    "startingDate": "2024-02-18"
  },
  {
    "code": "pointer",
    "title": "Pointer",
    "publicationCount": 4,
    "startingDate": "2024-02-20"
  }
]
```

### GET /rss/\[code]

Retrieve the RSS feed matching the give `code`.

Example:
```
GET http://localhost/rss/android_weekly

HTTP/1.1 200 OK
Connection: keep-alive
date: Sun, 03 Mar 2024 21:28:21 GMT
via: 1.1 alproxy
transfer-encoding: chunked

<?xml version="1.0" encoding="UTF-8"?>
<rss xmlns:dc="http://purl.org/dc/elements/1.1/" version="2.0">
  <channel>
    <title>Android Weekly</title>
    <link>https://androidweekly.net</link>
    <description>This is an RSS Feed for the newsletter "Android Weekly"</description>
    <item>
    ...
    </item>
  </channel>
</rss>
```

By default, only the articles of the latest 2 publications are retrieved. This number can be changed with URL parameter `publicationCount`

Example: `GET http://localhost/rss/android_weekly?publicationCount=4`


### POST /notifyRelease?version=1.2.3
This endpoint is used after a deployment to notify the analytics of the release. The release version is passed in the URL parameter `version`


### POST /stop
This endpoint is used to finalize a deployment by stopping the current instance of the application.

It is protected by a secret key (see the environment variables declared in [Deployment](#deployment))

## Tests

Tests can be run using the following command
```shell
$ ./gradlew check
```

`NewsletterHandler` implementations are tested with the emails stored in `src/main/resources/emails`.

The CI enforces a minimum coverage of 80%

## Deployment
This project needs access to an email account and a MariaDB database to run.

1. Build the project using the `build` command
  ```shell
  $ ./gradlew build
  ```

2. Copy files from the `deploy` folder into your server
  ```
  n2rss.jar
  application.properties
  ```

3. Declare the following environment variables on the server (some are optional)
  ```
  N2RSS_EMAIL_HOST=<host url of the email server>
  N2RSS_EMAIL_USERNAME=<username for the email server>
  N2RSS_EMAIL_PASSWORD=<password for the email server>
  N2RSS_EMAIL_PORT=<port of the email server. Default to 993>
  N2RSS_EMAIL_CRON=<CRON expression to check for new emails. Default to "0 0 * * * *">
  N2RSS_EMAIL_INBOX_FOLDERS=<Folders checked for new emails. Default to inbox>

  N2RSS_DISABLED_NEWSLETTERS=<code of disabled newsletters>

  N2RSS_SECRET_KEY=<secret key to interact with the maintenance endpoint>

  N2RSS_ANALYTICS_HOSTNAME=<website host sent to analytics, for server-side events>
  N2RSS_ANALYTICS_UA=<user-agent sent to analytics, for server-side events>

  N2RSS_DATABASE_URL=<JDBC url to the MariaDB database>
  N2RSS_DATABASE_USERNAME=<login used to connect to the database>
  N2RSS_DATABASE_PASSWORD=<password used to connect to the database>
  N2RSS_DATABASE_SCHEMA=<name of the schema to use in the database>

  N2RSS_GITHUB_ACCESS_TOKEN=<Token used to interact with GitHub to create issues>

  N2RSS_RECAPTCHA_SITE_KEY=<Site key to interact with reCaptcha API>
  N2RSS_RECAPTCHA_SECRET_KEY=<Secret key to interact with reCaptcha API>
  ```

Note on disabled newsletters: these newsletters do not appear on the Home screen, and emails are not processed.
The RSS feed is still accessible at the same URL but won't be updated until the newsletter is enabled again.

4. Run the following command to run the server
  ```shell
  java -jar n2rss.jar --server.address=:: --server.port=$PORT
  ```
  `$PORT` should be replaced by the port number the server should listen to

## Analytics

Analytics are handled by Simple Analytics, that respects the GDPR.
Most events are sent server-side, and no user-data are collected.

The following events are tracked:

- Access to the home page
- Access to the RSS feeds list
- Access to a specific RSS feed
- Request support for a new newsletter, with the newsletter URL
- Publication of a new n2rss release (sent from GitHub Actions CD)
- Any error detected (also in [Monitoring](#monitoring))

See `fr.nicopico.n2rss.analytics.models.AnalyticsEvent` for more details

## Monitoring

Any error will be reported as an issue on the GitHub repository defined by `n2rss.github.*` properties in
`application.properties` (configured to this repository by default), with the label "n2rss-bot".

## License

This project is licensed under the [MIT License](LICENSE.md)
