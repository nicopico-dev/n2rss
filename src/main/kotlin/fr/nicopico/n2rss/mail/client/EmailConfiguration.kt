package fr.nicopico.n2rss.mail.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("default")
@Configuration
class EmailConfiguration {

    @Value("\${EMAIL_HOST}")
    lateinit var host: String

    @Value("\${EMAIL_PORT}")
    var port: Int = 993

    @Value("\${EMAIL_USERNAME}")
    lateinit var username: String

    @Value("\${EMAIL_PASSWORD}")
    lateinit var password: String

    @Value("\${EMAIL_PROTOCOL}")
    var protocol: String = "imaps"

    @Value("\${EMAIL_INBOX_FOLDER}")
    var inboxFolder: String = "inbox"
}
