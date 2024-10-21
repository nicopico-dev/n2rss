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
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.should
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
        fun `should be able to process all the newsletter emails`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/Built for Mars")

            // WHEN - THEN
            shouldNotThrowAny {
                emails.forEach { email ->
                    handler.process(email)
                }
            }
        }

        @Test
        fun `should extract an article from an email (1)`() {
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

        // WHEN
        val publication = handler.process(email)

        // THEN
        publication.articles shouldHaveSize 5
        publication.articles shouldContainInOrder listOf(
            Article(
                title = "Euros 2024 celebration",
                link = URL("https://c.vialoops.com/CL0/https:%2F%2Fbuiltformars.com%2Fux-bites%2Feuros-2024-celebration/1/0100019092a99f62-c44d094f-0ed6-4a9a-87d4-a7c2748d2276-000000/z2-9ZzSn7NKjESxr4dt5Rebu0BiOiVwLyAnDjzCuDOk=360"),
                description = "If you Google a specific football result, you might see this celebratory overlay.",
            ),
            Article(
                title = "Casting a shadow",
                link = URL("https://c.vialoops.com/CL0/https:%2F%2Fbuiltformars.com%2Fux-bites%2Fcasting-a-shadow/1/0100019092a99f62-c44d094f-0ed6-4a9a-87d4-a7c2748d2276-000000/bVadl8McGE8cT9wTeyVNtJHqeFpJSTR68zPhMJjfFx8=360"),
                description = "As your Uber Eats driver moves along the map, they cast a shadow on the ground.",
            )
        )
    }

    @Test
    fun `should process UX Bites #36 email correctly`() {
        // GIVEN
        val email = loadEmail("stubs/emails/Built for Mars/UX Bites #36 — NBA, Octopus & Etsy.eml")

        // WHEN
        val publication = handler.process(email)

        // THEN
        publication.articles shouldHaveSize 5
        publication.articles[0] should {
            it.title shouldBe "Spoiler-free mode"
            it.description shouldBe "The NBA app will let you hide match spoilers as you browse."
        }
    }

    @Test
    fun `should process BFM #77 email correctly`() {
        // GIVEN
        val email = loadEmail("stubs/emails/Built for Mars/BFM #77 A masterclass in user activation \uD83D\uDE4C.eml")

        // WHEN
        val publication = handler.process(email)

        // THEN
        publication.articles shouldHaveSize 1
        publication.articles[0] should {
            it.title shouldBe "A masterclass in user activation \uD83D\uDE4C"
            it.description shouldBe """
                Hey 👋,
                
                Headspace: the orange-blob-face that tells me to breathe more slowly.
                
                They're experts at user activation.
                
                This is a masterclass. Get ready to learn some UX magic.
            """.trimIndent()
        }
    }
}
