package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NewsletterRepositoryTest {

    @MockK
    private lateinit var handler: NewsletterHandler

    private lateinit var repository: NewsletterRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        repository = NewsletterRepository(listOf(handler))
    }

    @Test
    fun `should return the newsletter for a given code`() {
        // GIVEN
        val code = "code"
        every { handler.newsletter.code } returns code

        // WHEN
        val actual = repository.findNewsletterByCode(code)

        // THEN
        actual shouldBe handler.newsletter
    }

    @Test
    fun `should return null if no newsletter correspond to the provided code`() {
        // GIVEN
        val code = "code"
        every { handler.newsletter.code } returns "foo"

        // WHEN
        val actual = repository.findNewsletterByCode(code)

        // THEN
        actual shouldBe null
    }
}
