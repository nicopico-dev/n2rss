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
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

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
        fun `should be able to process all the newsletter emails`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/MIT/The Download")

            // WHEN - THEN
            shouldNotThrowAny {
                emails.forEach { email ->
                    handler.process(email)
                }
            }
        }

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

            publication.articles.map { it.title } shouldBe listOf(
                "The messy quest to replace drugs with electricity",
                "Why bigger EVs aren’t always better",
                "House-flipping algorithms are coming to your neighborhood",
                "We can still have nice things",
            )

            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "The messy quest to replace drugs with electricity"
                }
                withClue("link") {
                    link shouldBe URL("https://technologyreview.us11.list-manage.com/track/click?u=47c1a9cec9749a8f8cbc83e78&id=d338c6f93e&e=4fc74d6331")
                }
                withClue("description") {
                    description shouldBe "In the early 2010s, electricity seemed poised " +
                        "for a hostile takeover of your doctor’s office. Research into how " +
                        "the nervous system—the highway that carries electrical messages between" +
                        " the brain and the body— controls the immune response was gaining " +
                        "traction. And that had opened the door to the possibility of hacking " +
                        "into the body’s circuitry and thereby controlling a host of chronic " +
                        "diseases, including rheumatoid arthritis, asthma, and diabetes, as if " +
                        "the immune system were as reprogrammable as a computer. To do that you’d " +
                        "need a new class of implant: an “electroceutical.” These devices would " +
                        "replace drugs. No more messy side effects. And no more guessing whether " +
                        "a drug would work differently for you and someone else. In the 10 years " +
                        "or so since, around a billion dollars has accreted around the effort. " +
                        "But electroceuticals have still not taken off as hoped. Now, however, " +
                        "a growing number of researchers are starting to look beyond the nervous " +
                        "system, and experimenting with clever ways to electrically manipulate " +
                        "cells elsewhere in the body, such as the skin., Their work suggests that " +
                        "this approach could match the early promise of electroceuticals, yielding " +
                        "fast-healing bioelectric bandages, novel approaches to treating autoimmune " +
                        "disorders, new ways of repairing nerve damage, and even better treatments " +
                        "for cancer. Read the full story., —Sally Adee"
                }
            }
        }

        @Test
        fun `should be able to process any mail from MIT The Download`() {
            // GIVEN
            val emails = loadEmails("stubs/emails/MIT/The Download")

            // WHEN - THEN
            shouldNotThrowAny {
                emails.forEach { email ->
                    println(email.subject)
                    handler.process(email)
                }
            }
        }

        @Test
        fun `should be able to process mail #139 from MIT The Download`() {
            // GIVEN
            val email = loadEmail("stubs/emails/MIT/The Download/139 Synthesia’s hype.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.articles shouldNot beEmpty()
        }

        @Test
        fun `should be able to process mail #144 from MIT The Download`() {
            // GIVEN
            val email = loadEmail("stubs/emails/MIT/The Download/144 How AI video gam.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.articles shouldNot beEmpty()
        }
    }
}
