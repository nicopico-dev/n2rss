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

package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class QuickBirdNewsletterHandlerTest {

    private lateinit var handler: QuickBirdNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = QuickBirdNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from QuickBird newsletter`() {
            // GIVEN
            val emails = loadEmails("emails/QuickBird Studios")

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
        fun `should extract an article from an email`() {
            // GIVEN
            val email: Email = loadEmail(
                "emails/QuickBird Studios/New blog post - Non-empty Lists in Kotlin.eml"
            )

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "New blog post - Non-empty Lists in Kotlin"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "QuickBird Studios"
                }
            }

            publication.articles shouldHaveSize 1
            publication.articles[0] should {
                withClue("title") {
                    it.title shouldBe "Non-empty Lists in Kotlin"
                }
                withClue("link") {
                    it.link shouldBe URL("https://quickbirdstudios.us17.list-manage.com/track/click?u=50a41d6b7ff8a128dd614fc40&id=fda8e7394e&e=64c2ecc47e")
                }
                withClue("description") {
                    it.description shouldBe "No-one likes to open an empty box – especially not at a birthday party! " +
                        "In our latest article, we construct non-empty lists and collections in Kotlin and use them " +
                        "to avoid such unpleasant surprises by design. \uD83D\uDE0E"
                }
            }
        }
    }
}
