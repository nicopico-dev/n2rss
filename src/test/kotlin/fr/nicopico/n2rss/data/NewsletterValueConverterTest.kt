package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.fakes.NewsletterHandlerFake
import fr.nicopico.n2rss.models.Newsletter
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

class NewsletterValueConverterTest {

    @Test
    fun `a newsletter should be serialized to its code`() {
        // GIVEN
        val converter = NewsletterValueConverter(emptyList())

        // WHEN
        val newsletter = Newsletter("code", "name", "websiteUrl")
        val result = converter.write(newsletter, mockk())

        // THEN
        result shouldBe "code"
    }

    @Test
    fun `a newsletter code should be deserialized to the corresponding newsletter`() {
        // GIVEN
        val newsletter = Newsletter("code", "name", "websiteUrl")
        val converter = NewsletterValueConverter(listOf(
            NewsletterHandlerFake("foo"),
            NewsletterHandlerFake(newsletter),
            NewsletterHandlerFake("bar"),
        ))

        // WHEN
        val result = converter.read("code", mockk())

        // THEN
        result shouldBe newsletter
    }

    @Test
    fun `a newsletter code should be deserialized to null if no match is found`() {
        // GIVEN
        val converter = NewsletterValueConverter(listOf(
            NewsletterHandlerFake("foo"),
            NewsletterHandlerFake("bar"),
        ))

        // WHEN
        val result = converter.read("code", mockk())

        // THEN
        result shouldBe null
    }
}
