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
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.isRegularFile

@Disabled("The base class tests cannot be executed directly")
open class BaseNewsletterHandlerTest<T : NewsletterHandler>(
    private val handlerProvider: () -> T,
    private val stubsFolder: String,
) {

    protected lateinit var handler: T
        private set

    @BeforeEach
    fun baseSetUp() {
        handler = handlerProvider()
    }

    @Nested
    inner class GenericEmailProcessingTest {
        @Test
        fun `should handle all emails from the newsletter`() {
            // GIVEN
            val emails = loadEmails("$STUBS_EMAIL_ROOT_FOLDER/$stubsFolder")

            // WHEN - THEN
            assertSoftly {
                emails.forEach { email ->
                    withClue("\"${email.subject}\" should be supported") {
                        handler.canHandle(email) shouldBe true
                    }
                }
            }
        }

        @Test
        fun `should ignore all emails from other newsletters`() {
            // GIVEN
            val emails = Files.walk(Paths.get(STUBS_EMAIL_ROOT_FOLDER))
                .filter {
                    it.isRegularFile()
                        && !it.startsWith(Paths.get("$STUBS_EMAIL_ROOT_FOLDER/$stubsFolder"))
                        && it.endsWith(".eml")
                }
                .flatMap {
                    try {
                        Stream.of(loadEmail(it.toString()))
                    } catch (e: NullPointerException) {
                        System.err.println("Error while processing $it -> ${e.message}")
                        Stream.empty()
                    }
                }

            // WHEN - THEN
            emails.anyMatch { handler.canHandle(it) } shouldBe false
        }

        @Test
        fun `should process all the newsletter emails without error`() {
            // GIVEN
            val emails = loadEmails("$STUBS_EMAIL_ROOT_FOLDER/$stubsFolder")

            // WHEN
            val articlesPerEmail = emails.associateWith { email ->
                handler.process(email).flatMap { it.articles }
            }

            // THEN
            assertSoftly {
                articlesPerEmail.forEach { (email, articles) ->
                    withClue("\"${email.subject}\" articles") {
                        articles shouldNot beEmpty()
                    }
                }
            }
        }
    }
}
