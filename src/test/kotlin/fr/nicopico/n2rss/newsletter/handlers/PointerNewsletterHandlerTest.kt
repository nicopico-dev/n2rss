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

package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.mail.models.Email
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
        fun `should handle all emails from Pointer`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Pointer")

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
        fun `should extract all articles from an email`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Pointer/Issue #480.eml")

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
                "SPONSOR - Gitpod: Built For Platform Teams",
                "Incentives And The Cobra Effect",
                "Applying The SPACE Framework",
                "How To Successfully Adopt A Developer Tool",
                "The Checklist Manifesto",
                "How Apple Built iCloud To Store Billions Of Databases",
                "The Ten Commandments Of Refactoring",
                "Dynamic Programming Is Not Black Magic",
                "How Fast Is Your Shell?",
            )
        }

        @Test
        fun `should extract article details from an email`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Pointer/Issue #480.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication.articles[1]) {
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

        @Test
        fun `should extract sponsor details from an email`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Pointer/Issue #480.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "SPONSOR - Gitpod: Built For Platform Teams"
                }
                withClue("link") {
                    link shouldBe URL("https://pointer.us9.list-manage.com/track/click?u=e9492ff27d760c578a39d0675&id=2191b13858&e=0e436c5282")
                }
                withClue("description") {
                    description shouldBe "Gitpod’s developer platform was built for developers looking to work faster and platform teams looking to work smarter. " +
                        "It allows them to do two things really well: automate standardization of development environments and always be ready-to-code. " +
                        "All it takes is adding a .gitpod.yml file to the root of any repository. " +
                        "Try Gitpod For Free"
                }
            }
        }

        @Test
        fun `should extract all articles from email #520`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Pointer/Issue #520.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "Issue #520"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Pointer"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "SPONSOR - FusionAuth: Don’t Build Your Own Auth. Try FusionAuth Today.",
                "How To Build Engineering Strategy",
                "Communication Structures",
                "How to Avoid Breached Passwords",
                "How To Test",
                "CLI Tricks Every Developer Should Know",
                "What Powersync Open Edition Means For Local-First",
                "Status Games",
                "What We’ve Learned From A Year of Building With LLMs",
                "A Virtual DOM In 200 Lines Of JavaScript",
            )
        }
    }
}
