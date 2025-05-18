/*
 * Copyright (c) 2025 Nicolas PICON
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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.beBlank
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class IosDevWeeklyNewsletterHandlerTest : BaseNewsletterHandlerTest<IosDevWeeklyNewsletterHandler>(
    handlerProvider = ::IosDevWeeklyNewsletterHandler,
    stubsFolder = "iOS Dev Weekly",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from iOS Dev Weekly Issue 705`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/iOS Dev Weekly/iOS Dev Weekly – Issue 705.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "iOS Dev Weekly – Issue 705"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "iOS Dev Weekly"
                }
            }

            // We expect articles from different sections: News, Tools, Code, etc.
            publication.articles shouldHaveSize 6

            // Verify articles from different sections
            withClue("Articles should contain expected titles") {
                publication.articles.map { it.title } shouldBe listOf(
                    "Is mobile the forgotten child of observability?",
                    "Tim, don't kill my vibe",
                    "How to automate perfect screenshots for the Mac App Store",
                    "Integrating Rust egui into SwiftUI",
                    "Deploying a Swift Server App",
                    "Profiling apps using Instruments"
                )
            }

            // Verify the first article (News section)
            assertSoftly(publication.articles[1]) {
                withClue("title") {
                    title shouldBe "Tim, don't kill my vibe"
                }
                withClue("link") {
                    link shouldBe URL("https://irace.me/vibe")
                }
                withClue("description") {
                    description shouldBe "Bryan Irace argues that as the time needed to create apps decreases, the process and time taken with everything after building the app looks more daunting."
                }
            }

            // Verify the second article (Tools section)
            assertSoftly(publication.articles[2]) {
                withClue("title") {
                    title shouldBe "How to automate perfect screenshots for the Mac App Store"
                }
                withClue("link") {
                    link shouldBe URL("https://www.jessesquires.com/blog/2025/03/24/automate-perfect-mac-screenshots/")
                }
                withClue("description") {
                    description shouldBe "Jesse Squires on automating capturing App Store screenshots for macOS apps, with great tips especially about processing the screenshots in Retrobatch."
                }
            }

            // Verify the third article (Code section)
            assertSoftly(publication.articles[2]) {
                withClue("title") {
                    title shouldBe "Integrating Rust egui into SwiftUI"
                }
                withClue("link") {
                    link shouldBe URL("https://medium.com/@djalex566/fast-fluid-integrating-rust-egui-into-swiftui-30a218c502c1")
                }
                withClue("description") {
                    description shouldBe "Oleksii Oliinyk's piece on integrating the egui graphics library with SwiftUI, used in his app Data Scout, which looks great if you need a modern SQLite/SwiftData database viewing tool."
                }
            }
        }

        @Test
        fun `should extract articles from iOS Dev Weekly Issue 706`() {
            // GIVEN
            val email: Email = loadEmail("stubs/emails/iOS Dev Weekly/iOS Dev Weekly – Issue 706.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "iOS Dev Weekly – Issue 706"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "iOS Dev Weekly"
                }
            }

            // The number of articles will depend on the actual content of Issue 706
            publication.articles.size shouldBe 7

            // Verify at least one article exists if there are any
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldNot beBlank()
                }
                withClue("link") {
                    link.toString() shouldStartWith "http"
                }
                withClue("description") {
                    description shouldNot beBlank()
                }
            }
        }
    }
}
