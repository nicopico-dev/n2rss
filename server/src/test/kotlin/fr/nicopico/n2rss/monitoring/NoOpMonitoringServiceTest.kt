/*
 * Copyright (c) 2025 Nicolas PICON
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
package fr.nicopico.n2rss.monitoring

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.EmailContent
import fr.nicopico.n2rss.mail.models.MessageId
import fr.nicopico.n2rss.mail.models.Sender
import fr.nicopico.n2rss.utils.now
import io.kotest.assertions.throwables.shouldNotThrowAny
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class NoOpMonitoringServiceTest {

    private lateinit var service: NoOpMonitoringService

    @BeforeEach
    fun setUp() {
        service = NoOpMonitoringService()
    }

    @Test
    fun testNotifyGenericError() {
        val exception = Exception("Test exception")
        shouldNotThrowAny {
            service.notifyGenericError(exception, "Test context")
        }
    }

    @Test
    fun testNotifyEmailProcessingError() {
        val email = Email(
            sender = Sender("sender@email.com"),
            date = LocalDate.now(),
            subject = "test",
            content = EmailContent.TextOnly("test"),
            messageId = MessageId("INBOX", 1),
        )
        val exception = Exception("Test exception")
        shouldNotThrowAny {
            service.notifyEmailProcessingError(email, exception)
        }
    }

    @Test
    fun testNotifyNewsletterRequest() {
        val url = URL("http://example.com")
        shouldNotThrowAny {
            service.notifyNewsletterRequest(url)
        }
    }
}
