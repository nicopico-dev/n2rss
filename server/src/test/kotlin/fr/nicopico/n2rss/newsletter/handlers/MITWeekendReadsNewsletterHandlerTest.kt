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
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class MITWeekendReadsNewsletterHandlerTest : BaseNewsletterHandlerTest<MITWeekendReadsNewsletterHandler>(
    handlerProvider = ::MITWeekendReadsNewsletterHandler,
    stubsFolder = "MIT/Weekend Reads"
) {

    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract an articles from an email`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/MIT/Weekend Reads/Robots + AI = New Frontiers! \uD83C\uDF10.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            assertSoftly(publication) {
                withClue("title") {
                    title shouldBe "Robots + AI = New Frontiers! \uD83C\uDF10"
                }
                withClue("date") {
                    date shouldHaveSameDayAs (email.date)
                }
                withClue("newsletter") {
                    newsletter.name shouldBe "MIT - Weekend Reads"
                }
            }

            publication.articles shouldHaveSize 9
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "Researchers taught robots to run. Now they’re teaching them to walk"
                }
                withClue("link") {
                    link shouldBe URL("https://technologyreview.us11.list-manage.com/track/click?u=47c1a9cec9749a8f8cbc83e78&id=4efec1a26b&e=4fc74d6331")
                }
                withClue("description") {
                    description shouldBe "Robots might need to become more boring to be useful."
                }
            }
        }

        @Test
        fun `should process email without article's descriptions correctly`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/MIT/Weekend Reads/NEW Our ambitious stories, narrated by real voice actors.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.articles shouldHaveSize 8
            assertSoftly(publication.articles[0]) {
                withClue("title") {
                    title shouldBe "What are AI agents?"
                }
                withClue("link") {
                    link shouldBe URL("https://technologyreview.us11.list-manage.com/track/click?u=47c1a9cec9749a8f8cbc83e78&id=35c51e4e4a&e=4fc74d6331")
                }
                withClue("description") {
                    description shouldBe ""
                }
            }

            publication.articles.map { it.title } shouldBe listOf(
                "What are AI agents?",
                "Is this the end of animal testing?",
                "How generative AI could reinvent what it means to play",
                "Is robotics about to have its own ChatGPT moment?",
                "This architect is cutting up materials to make them stronger and lighter",
                "The great commercial takeover of low Earth orbit",
                "It’s time to retire the term “user”",
                "The cost of building the perfect wave",
            )
        }
    }
}
