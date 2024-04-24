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
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `should extract all articles from an email`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Built for Mars/" + TODO("Email file name"))

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe TODO()
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Built for Mars"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                TODO()
            )
        }

        @Test
        fun `should extract article details from an email`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Built for Mars/" + TODO("Email file name"))

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe TODO()
                }
                withClue("link") {
                    link shouldBe TODO()
                }
                withClue("description") {
                    description shouldBe TODO()
                }
            }
        }
    }
}
