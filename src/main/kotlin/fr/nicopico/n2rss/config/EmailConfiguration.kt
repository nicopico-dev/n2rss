/*
 * Copyright (c) 2024 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fr.nicopico.n2rss.config

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.client.JavaxEmailClient
import fr.nicopico.n2rss.mail.client.NoOpEmailClient
import fr.nicopico.n2rss.mail.client.ResourceFileEmailClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class EmailConfiguration {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @Profile("default")
    fun emailClient(config: EmailParameters): EmailClient {
        return JavaxEmailClient(
            protocol = config.protocol,
            host = config.host,
            port = config.port,
            user = config.username,
            password = config.password,
            inboxFolder = config.inboxFolder,
        )
    }

    @Bean
    @Profile("local")
    fun fakeEmailClient(): EmailClient = ResourceFileEmailClient("emails")

    @Bean
    @Profile("rss-only")
    fun noopEmailClient(): EmailClient = NoOpEmailClient()
}

@Profile("default")
@Configuration
class EmailParameters {

    companion object {
        const val DEFAULT_PORT = 993
        const val DEFAULT_PROTOCOL = "imaps"
        const val DEFAULT_INBOX_FOLDER = "inbox"
    }

    @Value("\${EMAIL_HOST}")
    lateinit var host: String

    @Value("\${EMAIL_PORT}")
    var port: Int = DEFAULT_PORT

    @Value("\${EMAIL_USERNAME}")
    lateinit var username: String

    @Value("\${EMAIL_PASSWORD}")
    lateinit var password: String

    @Value("\${EMAIL_PROTOCOL}")
    var protocol: String = DEFAULT_PROTOCOL

    @Value("\${EMAIL_INBOX_FOLDER}")
    var inboxFolder: String = DEFAULT_INBOX_FOLDER
}
