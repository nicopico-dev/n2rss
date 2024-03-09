package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoOpNewsletterHandlerTest {

    private lateinit var handler: NoOpNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = NoOpNewsletterHandler()
    }

    @Test
    fun canHandle() {
        // GIVEN
        val anyEmail = mockk<Email>()

        // WHEN
        val result = handler.canHandle(anyEmail)

        // THEN
        result shouldBe true
    }

    @Test
    fun extractArticles() {
        // GIVEN
        val anyEmail = mockk<Email>()

        // WHEN
        val result = handler.extractArticles(anyEmail)

        // THEN
        result should beEmpty()
    }
}
