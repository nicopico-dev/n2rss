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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class PointerNewsletterHandlerTest : BaseNewsletterHandlerTest<PointerNewsletterHandler>(
    handlerProvider = ::PointerNewsletterHandler,
    stubsFolder = "Pointer",
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email #480`() {
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
        fun `should extract article details from email #480`() {
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
        fun `should extract sponsor details from email #480`() {
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

        @Test
        fun `should process email #541`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/Pointer/#541.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "#541"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Pointer"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "SPONSOR - Start Selling To Enterprises With Just A Few Lines Of Code",
                "Practices Of Reliable Software Design",
                "How to Write Great Tech Specs",
                "Build Vs Buy Part I: Complexities Of Building SSO And SCIM In-House",
                "The 3 Motivational Forces Of Developers",
                "Onboarding To A ‘Legacy' Codebase With The Help Of AI",
                "How Developers Really Use AI",
                "Crazy Debugging Stories - Recursion",
                "Real-Time Mouse Pointers",
                "Sort, Sweep, And Prune: Collision Detection Algorithms",
            )

            withClue("Sponsor description") {
                publication.articles[0].description shouldBe """
                    WorkOS is a modern identity platform for B2B SaaS.
                    With modular and easy-to-use APIs, integrate complex features like SSO and SCIM in minutes of months.
                    If you care deeply about design and user experience, WorkOS is a perfect fit. From high-quality documentation to self-serve onboarding for your customers, WorkOS removes all the complexity for your engineers.
                    User Management is also free up to 1 million MAUs and includes MFA, bot protection, domain verification.
                """.trimIndent()
            }

            withClue("Article description") {
                publication.articles[1].description shouldBe """
                    — Christoffer Stjernlöf
                    tl;dr: Christoffer discusses the following: (1) Use off-the-shelf. (2) Cost and reliability over features. (3) Idea to production quickly. (4) Simple data structures. (5) Reserve resources early. (6) Set maximums. (7) Make testing easy. (8) Embed performance counters.
                """.trimIndent()
            }
        }
    }
}
