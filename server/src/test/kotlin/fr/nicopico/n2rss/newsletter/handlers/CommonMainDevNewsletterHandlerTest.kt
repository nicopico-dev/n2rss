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

import fr.nicopico.n2rss.STUBS_EMAIL_ROOT_FOLDER
import fr.nicopico.n2rss.mail.models.Email
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CommonMainDevNewsletterHandlerTest : BaseNewsletterHandlerTest<CommonMainDevNewsletterHandler>(
    handlerProvider = ::CommonMainDevNewsletterHandler,
    stubsFolder = "commonMain.dev",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email sample`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/commonMain.dev/Kotlin Multiplatform Newsletter #16.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            val mainPublication = publications.first { it.newsletter == CommonMainDevNewsletterHandler.mainNewsletter }
            val librariesPublication =
                publications.first { it.newsletter == CommonMainDevNewsletterHandler.librariesNewsletter }

            assertSoftly {
                mainPublication.articles shouldHaveSize 15
                withClue("main titles") {
                    mainPublication.articles.map { it.title } shouldBe listOf(
                        "Is Kotlin Multiplatform (KMP) actually worth using in 2026?",
                        "SPONSORED - Practical Kotlin Deep Dive",
                        "Migrating from Koin DSL to Koin Annotations in a Multimodule Project: A Step-by-Step Guide",
                        "Managing Gradle Daemons while Coding with AI",
                        "Threads, Mutexes, Semaphores, Dispatchers & Parallelism: The Mental Model Most Engineers Are Still Missing",
                        "Building a Production-Ready Kotlin Multiplatform SDK for Android & iOS",
                        "The Clean Line: Swift Export for KMP",
                        "Compose Performance Skills",
                        "Haze 2.0: A Pluggable Visual Effects Engine",
                        "SPONSORED - Carrd",
                        "OptyMacros - Optimize your diet. Eat what you want.",
                        "GitHub - terrakok/CozySpace",
                        "Better: Gym & Calorie Tracker - Apps on Google Play",
                        "SPONSORED - wellfound",
                        "OTX Runtime Engineer",
                    )
                }

                librariesPublication.articles shouldHaveSize 3
                withClue("libraries titles") {
                    librariesPublication.articles.map { it.title } shouldBe listOf(
                        "Pulsar",
                        "Colormath",
                        "KMPUtils",
                    )
                }
            }
        }
    }
}
