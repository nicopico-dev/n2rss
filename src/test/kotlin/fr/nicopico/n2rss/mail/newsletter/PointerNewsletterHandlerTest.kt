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

class PointerNewsletterHandlerTest {

    private lateinit var handler: PointerNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = PointerNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from AndroidWeekly`() {
            // GIVEN
            val emails = loadEmails("emails/Pointer")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe true
        }

        @Test
        fun `should ignore all emails from another newsletters`() {
            // GIVEN
            val emails = loadEmails("emails/Kotlin Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }
    }

    @Nested
    inner class ProcessTest {
        @Test
        fun `should extract all articles from an Android Weekly email`() {
            // GIVEN
            val email: Email = loadEmail("emails/Pointer/Issue #480.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "Issue #480"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Pointer"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "Incentives And The Cobra Effect",
                "Applying The SPACE Framework",
                "How To Successfully Adopt A Developer Tool",
                "The Checklist Manifesto",
                "How Apple Built iCloud To Store Billions Of Databases",
                "The Ten Commandments Of Refactoring",
                "Dynamic Programming Is Not Black Magic",
                "How Fast Is Your Shell?",
            )

            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "Incentives And The Cobra Effect"
                }
                withClue("link") {
                    link shouldBe URL("https://pointer.us9.list-manage.com/track/click?u=e9492ff27d760c578a39d0675&id=d20cd3411a&e=0e436c5282")
                }
                withClue("description") {
                    description shouldBe "“Incentives are superpowers; set them carefully.” The Cobra Effect is when the solution for a problem unintentionally makes the problem worse. Andrew believe this issue is more widespread than anticipated. He provides several examples, including: everyone sharing feedback directly instead of through managers. This leads to people withholding valuable feedback to maintain relationships or damaging relationships if they can’t share negative feedback elegantly."
                }
            }
        }
    }
}
