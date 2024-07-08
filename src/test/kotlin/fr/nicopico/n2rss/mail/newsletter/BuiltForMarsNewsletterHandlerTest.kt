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
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class BuiltForMarsNewsletterHandlerTest {

    private lateinit var handler: BuiltForMarsNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = BuiltForMarsNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from Built for Mars`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Built for Mars")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe true
        }

        @Test
        fun `should ignore all emails from another newsletters`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Android Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }
    }

    @Nested
    inner class ProcessTest {
        @Test
        fun `should process any Built for Mars email`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Built for Mars")

            // WHEN - THEN
            shouldNotThrowAny {
                for (email in emails) {
                    handler.process(email)
                }
            }
        }

        @Test
        fun `should extract an articles from an email (1)`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/Built for Mars/BFM #72 Why giving away free stocks isn't easy.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "BFM #72: Why giving away free stocks isn't easy \uD83D\uDCC8"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Built for Mars"
                }
            }

            publication.articles shouldHaveSize 1
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "Why giving away free stocks isn't easy \uD83D\uDCC8"
                }
                withClue("link") {
                    link shouldBe URL("https://link.mail.beehiiv.com/ss/c/u001.IvR796000sSoCuC84EAfml9aijFF9Py_qFz_4rcpI0eKyy7lMMq9JlJa5WIULczOai9fDIIg4Nar_zOXSruEqw/460/vdO90HyzTomWFLK4lA7zDw/h2/h001.VjqhHF95LKAUUesIfc6_N5ycfCTeJwcSQDkHHhiKvts")
                }
                withClue("description") {
                    description shouldBe """Hey 👋

“And we’ll launch our start-up with a viral referral scheme”.

Despite being the daring plan of almost every start-up, it’s got a success rate so low that it’s usually just a large distraction.

Well, Robinhood are one of the exceptions.

Of course, luck and branding plays a role. But they’re enabled by very predictable psychological biases, nudges, hooks and design cues.

In this study, I’ve tried to break down these subtleties, and explain exactly why people are so drawn to their ${'$'}7.08 welcome bonus.""".trimIndent()
                }

            }
        }
    }

    @Test
    fun `should process email #22`() {
        // GIVEN
        val email = loadEmail("stubs/emails/Built for Mars/UX Bites #22 — Monzo, Uber & Booking.eml")

        // WHEN - THEN
        shouldNotThrowAny {
            handler.process(email)
        }
    }
}
