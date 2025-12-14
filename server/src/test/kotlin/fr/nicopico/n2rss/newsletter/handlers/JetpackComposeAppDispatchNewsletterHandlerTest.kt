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
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class JetpackComposeAppDispatchNewsletterHandlerTest :
    BaseNewsletterHandlerTest<JetpackComposeAppDispatchNewsletterHandler>(
        handlerProvider = ::JetpackComposeAppDispatchNewsletterHandler,
        stubsFolder = "JetpackCompose app Dispatch",
    ) {
    @Nested
    inner class EmailProcessingTest {
        @Test
        fun `should extract articles from issue #16`() {
            // GIVEN
            val email: Email =
                loadEmail("$STUBS_EMAIL_ROOT_FOLDER/JetpackCompose app Dispatch/\uD83D\uDE80 JetpackCompose.app's Dispatch Issue #16 _ \uD83D\uDDD1\uFE0F Google Play’s app-purge drama, \uD83D\uDEE0\uFE0F Amazon’s shiny new KMP “App Platform”, \uD83C\uDFA8 the th.eml")

            // WHEN
            val publication = handler.process(email)

            // THEN
            publication.articles shouldNot beEmpty()

            publication.articles.first() shouldBe Article(
                title = "\uD83D\uDD75\uD83C\uDFFB\u200D♂\uFE0F Insider Insight",
                link = URL("https://link.mail.beehiiv.com/ss/c/u001.DgkFNI5fx5JE22pMoCZFwAQ-1zlkWP0Jty0jQqcdt_a2lHFOLrgu1J5pnfGxivDh1bu08LrXULBcKBupQQstI2r9ppD4qfw9ArdWSRQ3RuAvTgrXTeXb0qAhSFJhxZQfVN_jXfZT548kwZlHMKERTkAnOEu_9732Nqb8W2-6CgYLW_ZF3Ap9nC1FvQ9e0mLWywWfwDcdtaoaAdYszP8_MAtnylCdEvMFet3P4jxJEHar9BV0Eu1WMVznhn5lKV-2CQ-t8cssgymuKBJKwzVvV_XedgQ5ziYuy7eFIjfp384x5NVHx9iMrZ-x8XJjYv_t/4gm/VVHcxVIJQxSWZuj7UGYW3Q/h0/h001.6xURNvbt0xouCcrKYzlPTQkhUsH1rBFhz0aItAmem1M"),
                description = "TODO !",
            )
        }
    }
}
