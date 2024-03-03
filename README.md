# N2RSS

N2RSS (*Newsletter to RSS*) goal is to publish RSS feeds containing separate articles
extracted from selected newsletters.

| Newsletter     | URL                       | Status  |
|----------------|---------------------------|---------|
| Android Weekly | https://androidweekly.net | OK      |
| Pointer        | https://www.pointer.io    | OK      |
| Kotlin Weekly  | http://kotlinweekly.net   | Pending |

## Built With

- Jakarta EE
- Spring MVC
- Spring Data MONGO
- Kotlin API version 1.9
- Java SDK version 17

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

- [ ] Provide a step-by-step guide to get your development environment running.

## Tests

Tests can be run using the following command
```shell
$ ./gradlew check
```

`NewsletterHandler` implementations are tested with the emails stored in `src/main/resources/emails`

## Usage

- [ ] Examples of how to use this project. Could be code examples or screenshots.

## Deployment
This project need access to an email account and a MongoDB database to run.

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
   ```
4. Run the following command to run the server
   ```shell
   java -jar n2rss.jar --server.address=:: --server.port=$PORT
   ```
   `$PORT` should be replaced by the port number the server should listen to 

## Authors

- Nicolas PICON

## License

This project is licensed under the [MIT License](LICENSE.md)
