package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.mail.models.Email
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoOpEmailClientTest {

    private lateinit var emailClient: NoOpEmailClient

    @BeforeEach
    fun setUp() {
        emailClient = NoOpEmailClient()
    }

    @Test
    fun markAsRead() {
        // GIVEN
        val anyEmail = mockk<Email>()

        // WHEN - THEN
        shouldNotThrowAny {
            emailClient.markAsRead(anyEmail)
        }
    }

    @Test
    fun checkEmails() {
        // WHEN
        val result = emailClient.checkEmails()

        // THEN
        result should beEmpty()
    }
}
