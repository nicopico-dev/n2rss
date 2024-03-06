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
import java.net.URL

class AndroidWeeklyNewsletterHandlerTest {

    private lateinit var handler: AndroidWeeklyNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = AndroidWeeklyNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from AndroidWeekly`() {
            // GIVEN
            val emails = loadEmails("emails/Android Weekly")

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
            val email: Email = loadEmail("emails/Android Weekly/Android Weekly #605.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "Android Weekly #605 \uD83E\uDD16"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "Android Weekly (Articles only)"
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "How to add text similarity easily using MediaPipe and Kotlin",
                "Safely Navigating the Transition: From Gson to kotlinx.serialization",
                "Creating and managing custom-scoped components in Dagger + Anvil",
                "Camouflage the Status Bar with Edge-to-Edge Jetpack Compose Dialogs",
                "Why use Flow if we have the powerful ChannelFlow in mobile development?",
                "Exploring Health Connect Pt. 1 - Setting Up Permissions",
                "Running UI tests in Jetpack Compose using Firebase Test Lab",
                "Kotlin-Swift interopedia",
                "Keep Your Kotlin Code Spotless: A Guide to ktlint and ktfmt Linters",
            )

            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "How to add text similarity easily using MediaPipe and Kotlin"
                }
                withClue("link") {
                    link shouldBe URL("https://r20.rs6.net/tn.jsp?f=001YHdDWcIocRmEUng2FyDIZCM865jNXQCI2Rqy-2M-vNbfJdIgMHvRtHYbATstN3kGB9HprhnbPYLVzFxoewUaaejGyLe52auChGpRvDynAVEWc1A1wSoy8ZmiqmqNJhMuf67y5eG7ydf1OTZPZPB4FPQwqWT2LK-6hXWhyH0Js9DME5DujnMrIvbnalUKlch86KDIzPj-UlGsMphBYKA9IkE2nX46rr-9gj_u2vZ0H8syGAECFgg4qV3ZXxfCkGe84XXhoCcTWt8ZhrNzT0JJEx5rz84rYNdXtCOSyPEJHNKOYDOg9-X6Zg==&c=zrzdfNBtlG2zX5Iq8EiYBmRjj1u-3dLoDyskVZ8jzgoLL4bugqTz3Q==&ch=b-W9Yhs6lzvdh-BOht77aRzv1XT0juBV2IBbXPI7zM490y0EfdXTUA==")
                }
                withClue("description") {
                    description shouldBe "Juan Guillermo Gómez Torres takes a look at MediaPipe Solutions, which provides a set of libraries and tools to apply machine learning (ML) to your applications quickly."
                }
            }
        }
    }
}
