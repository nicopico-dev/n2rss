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
import fr.nicopico.n2rss.newsletter.models.Article
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class [NewsletterName]NewsletterHandlerTest : BaseNewsletterHandlerTest<[NewsletterName]NewsletterHandler>(
handlerProvider = ::[NewsletterName]NewsletterHandler,
stubsFolder = "[NewsletterName]",
) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract all articles from email sample`() {
            // GIVEN
            val email: Email = loadEmail("$STUBS_EMAIL_ROOT_FOLDER/[NewsletterName]/Sample.eml")

            // WHEN
            val publications = handler.process(email)

            // THEN
            publications shouldHaveSize 2

            val mainPublication =
                publications.first { it.newsletter == [NewsletterName] NewsletterHandler . mainNewsletter }
            assertSoftly {
                withClue("Main Newsletter - titles") {
                    mainPublication.articles.map { it.title } shouldBe listOf(
                        "Expected Main Article 1 Title",
                        // ...
                    )
                }
            }

            val secondaryPublication =
                publications.first { it.newsletter == [NewsletterName] NewsletterHandler . secondaryNewsletter }
            assertSoftly {
                withClue("Secondary Newsletter - titles") {
                    secondaryPublication.articles.map { it.title } shouldBe listOf(
                        "Expected Secondary Article 1 Title",
                        // ...
                    )
                }
            }
        }
    }
}
