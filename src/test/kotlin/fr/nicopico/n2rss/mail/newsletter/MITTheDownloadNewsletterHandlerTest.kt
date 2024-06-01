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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MITTheDownloadNewsletterHandlerTest {

    private lateinit var handler: MITTheDownloadNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = MITTheDownloadNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from MIT - The Download`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/MIT/The Download")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe true
        }

        @Test
        fun `should ignore all emails from another newsletters`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Kotlin Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }
    }

    @Nested
    inner class ProcessTest {
        @Test
        fun `should extract an articles from an email`() {
            // GIVEN
            val email: Email =
                loadEmail("stubs/emails/MIT/The Download/The messy quest to replace drugs with electricity.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "The messy quest to replace drugs with electricity"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "MIT - The Download"
                }
            }

            publication.articles shouldHaveSize 0
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe ""
                }
                withClue("link") {
                    link shouldBe ""
                }
                withClue("description") {
                    description shouldBe ""
                }
            }
        }
    }
}
