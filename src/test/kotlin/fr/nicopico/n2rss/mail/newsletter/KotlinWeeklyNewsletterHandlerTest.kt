package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class KotlinWeeklyNewsletterHandlerTest {

    private lateinit var handler: KotlinWeeklyNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = KotlinWeeklyNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from AndroidWeekly`() {
            // GIVEN
            val emails = loadEmails("emails/Kotlin Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe true
        }

        @Test
        fun `should ignore all emails from another newsletters`() {
            // GIVEN
            val emails = loadEmails("emails/Android Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }
    }

    @Nested
    inner class ProcessTest {
        @Test
        fun `should extract all articles from an Android Weekly email`() {
            // GIVEN
            val email: Email = loadEmail("emails/Kotlin Weekly/Kotlin Weekly #388.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "Kotlin Weekly #388"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Kotlin Weekly"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "Comparing coroutines, by example, in Kotlin and Python",
                "How To Use kotlinx.serialization with Ktor and Kotlin?",
                "Synchronous and Asynchronous runs: run, runCatching, runBlocking and runInterruptible in Kotlin",
                "Structured Concurrency for Coroutines: Unraveling the Fundamentals",
            )

            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "Comparing coroutines, by example, in Kotlin and Python"
                }
                withClue("link") {
                    link shouldBe URL("https://kotlinweekly.us12.list-manage.com/track/click?u=f39692e245b94f7fb693b6d82&id=c84ddcbb09&e=8523a5b059")
                }
                withClue("description") {
                    description shouldBe "Carmen Álvarez wrote an article comparing Coroutines under a Kotlin and Python prism."
                }
            }
        }
    }
}
