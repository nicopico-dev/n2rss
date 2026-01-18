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
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class KotlinWeeklyNewsletterHandlerTest : BaseNewsletterHandlerTest<KotlinWeeklyNewsletterHandler>(
    handlerProvider = ::KotlinWeeklyNewsletterHandler,
    stubsFolder = "Kotlin Weekly"
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email #388`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kotlin Weekly/Kotlin Weekly #388.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            assertSoftly(publications[0]) {
                withClue("title") {
                    title shouldBe "Kotlin Weekly #388"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Kotlin Weekly"
                }
                withClue("notes") {
                    newsletter.notes shouldBe "Articles"
                }
                withClue("articles") {
                    articles.map { it.title } shouldBe listOf(
                        "Comparing coroutines, by example, in Kotlin and Python",
                        "How To Use kotlinx.serialization with Ktor and Kotlin?",
                        "Synchronous and Asynchronous runs: run, runCatching, runBlocking and runInterruptible in Kotlin",
                        "Structured Concurrency for Coroutines: Unraveling the Fundamentals",
                    )
                }
            }
        }

        @Test
        fun `should extract all articles from email #390`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kotlin Weekly/Kotlin Weekly #390.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication[0]) {
                withClue("title") {
                    title shouldBe "Kotlin Weekly #390"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Kotlin Weekly"
                }
                withClue("articles") {
                    articles.map { it.title } shouldBe listOf(
                        "Learn IDE Code Refactoring in Kotlin for Enhanced Code Quality",
                        "IntelliJ IDEA’s K2 Kotlin Mode Now in Alpha",
                        "KotlinConf’24 is 80% sold out",
                        "Sealed Types",
                        "How To Create a Ktor Client To Connect To OpenWeatherMap API",
                        "Micro-optimizations in Kotlin — 1",
                        "Challenge: Shop orders processing",
                        "How to add text similarity to your Android applications easily using MediaPipe and Kotlin",
                        "SPONSORED - The future of intelligent composable content",
                        "Jetpack Compose: The Android Developer Roadmap – Part 5",
                    )
                }
            }
        }

        @Test
        fun `should extract article details from email #388`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kotlin Weekly/Kotlin Weekly #388.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication[0].articles[0]) {
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

        @Test
        fun `should parse Kotlin Weekly #400 without issue`() {
            // GIVEN
            val email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kotlin Weekly/Kotlin Weekly #400.eml")

            // WHEN - THEN
            val publication = shouldNotThrowAny {
                handler.process(email)
            }

            publication[0].articles.map { it.title } shouldBe listOf(
                "Kotlin Today Magazine launched!",
                "K2 Kotlin Mode (Alpha) in IntelliJ IDEA",
                "Why Non-Blocking?",
                "Data Analytics With Kotlin Notebooks, DataFrame, and Kandy",
                "Generics",
                "Kotlin/Wasm interop with Javascript",
                "Introduction to using Kotlin Serialization",
                "SPONSORED - Build local-first KMP apps with PowerSync",
                "Roboto Conference: where tech meets beauty",
            )
        }

        @Test
        fun `should extract all libraries from email #388`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kotlin Weekly/Kotlin Weekly #388.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            assertSoftly(publications[1]) {
                withClue("title") {
                    title shouldBe "Kotlin Weekly #388"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Kotlin Weekly"
                }
                withClue("notes") {
                    newsletter.notes shouldBe "Libraries"
                }
                withClue("libraries") {
                    articles.map { it.title } shouldBe listOf(
                        "GeminiKMP",
                        "JNAV : Jetpack compose Navigation",
                        "Kottie",
                        "PPrint for Kotlin",
                        "KodeRunner",
                        "geminichat",
                        "JaCoCo Aggregate Coverage Plugin",
                    )
                }

                withClue("First library") {
                    assertSoftly(articles[0]) {
                        withClue("title") {
                            title shouldBe "GeminiKMP"
                        }
                        withClue("link") {
                            link shouldBe URL("https://kotlinweekly.us12.list-manage.com/track/click?u=f39692e245b94f7fb693b6d82&id=0b3825886d&e=8523a5b059")
                        }
                        withClue("description") {
                            description shouldBe "John O'Reilly, the serial KMP content creator, ended 2023 releasing GeminiKMP, a Kotlin Multiplatform sample that uses Gemini Generative AI APIs."
                        }
                    }
                }
            }
        }

        @Test
        fun `should extract all articles & libraries from email #494`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/Kotlin Weekly/Kotlin Weekly #494.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            withClue("articles") {
                publications[0].articles.map { it.title } shouldBe listOf(
                    "The Journey to Compose Hot Reload 1.0.0",
                    "Dependency Injection with Koin, the minimal setup (KMP)",
                    "Update your Kotlin projects for Android Gradle Plugin 9.0",
                    "Explore internal mechanisms of Retrofit, and how it works",
                    "How I’m Using Agents for Development - January 2026",
                    "Backpressure in Client-Server Applications",
                    "Setting up Kotest on KMP",
                    "SPONSORED - Compose courses launch next week",
                    "300 - From Vibe coding to Software engineering",
                )
            }

            assertSoftly(publications[1]) {
                withClue("title") {
                    title shouldBe "Kotlin Weekly #494"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Kotlin Weekly"
                }
                withClue("notes") {
                    newsletter.notes shouldBe "Libraries"
                }
                withClue("libraries") {
                    articles.map { it.title } shouldBe listOf(
                        "Resilient - Kotlin Multiplatform Library",
                        "Llamatik",
                        "Nominatim Proxy",
                        "Kotlin Multiplatform State Machine",
                    )
                }
            }
        }

        @Test
        @Disabled("Need a Kotlin Weekly Stub without any libraries")
        fun `should not publish on library feed if there is no libraries in Kotlin Weekly email`() {
            // GIVEN
            val email: Email = loadEmail("TODO")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 1
            publications[0].newsletter shouldBe KotlinWeeklyNewsletterHandler.defaultNewsletter
        }
    }
}
