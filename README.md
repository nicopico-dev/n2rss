# N2RSS

N2RSS (*Newsletter to RSS*) goal is to publish RSS feeds containing separate articles
extracted from selected newsletters.

It will run an email check periodically for new emails on an inbox.
A recognized email will be processed, while unrecognized email are left as-is.
Once an email has been processed, it is marked as read.

Each newsletter will map to a separate RSS feed, 
and each articles extracted from the newsletter publication will map to different RSS entry in the corresponding feed.

| Newsletter     | URL                       | Status |
|----------------|---------------------------|--------|
| Android Weekly | https://androidweekly.net | OK     |
| Kotlin Weekly  | http://kotlinweekly.net   | OK     |
| Pointer        | https://www.pointer.io    | OK     |

## Built With

- Kotlin 1.9
- Java 17
- Jakarta EE
- Spring MVC
- Spring Data MONGO
- JSOUP to parse email content
- ROME to generate RSS feeds

## Getting Started

These instructions will get you a copy of the project 
up and running on your local machine for development 
and testing purposes.

### Prerequisites

- Java SDK version 17
- Kotlin API version 1.9
- IDE that supports Jakarta EE, Spring, Java and Kotlin development (For example: IntelliJ IDEA)
- Docker (for development purpose only)

### Installing

This project is a Spring Boot application written in Kotlin

By default, it will connect an `EmailClient` using parameters provided as environment variables:
```
EMAIL_HOST=<host url of the email account>
EMAIL_USERNAME=<username for the email account>
EMAIL_PASSWORD=<password for the email account>
EMAIL_INBOX_FOLDER=inbox
EMAIL_PORT=993
EMAIL_PROTOCOL=imaps
```

By using the `local` profile, a `ResourceFileEmailClient` will be used instead and use the files located 
at `src/main/resources/emails`. This profile is recommended to get a faster feedback-loop while developing.

```shell
$ ./gradlew bootRun --args='--spring.profiles.active=local'
```

When run for development through an IDE or with `bootRun`, Spring Boot will use a Docker container 
to run the **MongoDB database**. This Docker container will be created automatically on the first run, 
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
    "publicationCount": 3
  },
  {
    "code": "pointer",
    "title": "Pointer",
    "publicationCount": 4
  }
]
```

### GET /rss/\[code]

Retrieve the RSS feed matching the give `code`.
By default, only the articles of the latest 2 publications are retrieved.

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

### POST /stop
This endpoint is used to finalize a deployment by stopping the current instance of the application.

It is protected by a secret key (see the environment variables declared in [Deployment](#deployment))

## Tests

Tests can be run using the following command
```shell
$ ./gradlew check
```

`NewsletterHandler` implementations are tested with the emails stored in `src/main/resources/emails`.

The CI enforce a minimum coverage of 80%

## Deployment
This project needs access to an email account and a MongoDB database to run.

1. Build the project using the `build` command
   ```shell
    $ ./gradlew build
    ```
2. Copy files from the `deploy` folder into your server
   ```
   n2rss.jar
   application.properties
   ```
3. Declare the following environment variables on the server
   ```
   EMAIL_HOST=<host url of the email account>
   EMAIL_USERNAME=<username for the email account>
   EMAIL_PASSWORD=<password for the email account>
   EMAIL_INBOX_FOLDER=inbox
   EMAIL_PORT=993
   EMAIL_PROTOCOL=imaps
   
   MONGODB_HOST=<host of the mongodb database>
   MONGODB_USERNAME=<username for the mongodb database>
   MONGODB_PASSWORD=<password for the mongodb database>
   MONGODB_DATABASE=<name of the database to use>
   MONGODB_PORT=27017
   
   N2RSS_SECRET_KEY=<secret key to interact with /stop endpoint>
   ```
4. Run the following command to run the server
   ```shell
   java -jar n2rss.jar --server.address=:: --server.port=$PORT
   ```
   `$PORT` should be replaced by the port number the server should listen to

## License

This project is licensed under the [MIT License](LICENSE.md)
