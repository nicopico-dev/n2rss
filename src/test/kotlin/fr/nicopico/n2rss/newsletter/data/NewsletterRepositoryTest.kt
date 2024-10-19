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

package fr.nicopico.n2rss.newsletter.data

import fr.nicopico.n2rss.fakes.NewsletterHandlerFake
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.newsletters
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class NewsletterRepositoryTest {

    private lateinit var repository: NewsletterRepository

    private fun createRepository(handler: NewsletterHandler) {
        repository = NewsletterRepository(listOf(handler), listOf(handler))
    }

    @Test
    fun `should return the newsletter for a given code`() {
        // GIVEN
        val code = "code"
        val handler = NewsletterHandlerFake(code)
        createRepository(handler)

        // WHEN
        val actual = repository.findNewsletterByCode(code)

        // THEN
        actual shouldBeIn handler.newsletters
        actual?.code shouldBe code
    }

    @Test
    fun `should return null if no newsletter correspond to the provided code`() {
        // GIVEN
        val code = "code"
        createRepository(NewsletterHandlerFake("foo"))

        // WHEN
        val actual = repository.findNewsletterByCode(code)

        // THEN
        actual shouldBe null
    }

    @Test
    fun `should return all enabled newsletters`() {
        // GIVEN
        val allHandlers = listOf(
            NewsletterHandlerFake("code1"),
            NewsletterHandlerFake("code2"),
            NewsletterHandlerFake("code3"),
        )
        val enabledHandlers = allHandlers.take(2)
        repository = NewsletterRepository(
            allNewsletterHandlers = allHandlers,
            enabledNewsletterHandlers = enabledHandlers,
        )

        // WHEN
        val actual = repository.getEnabledNewsletters()

        // THEN
        actual.map { it.code } shouldBe listOf("code1", "code2")
    }

    @Test
    fun `should return enabled newsletter handlers`() {
        // GIVEN
        val allHandlers = listOf(
            NewsletterHandlerFake("code1"),
            NewsletterHandlerFake("code2"),
            NewsletterHandlerFake("code3"),
        )
        val enabledHandlers = allHandlers.take(1)
        repository = NewsletterRepository(
            allNewsletterHandlers = allHandlers,
            enabledNewsletterHandlers = enabledHandlers,
        )

        // WHEN
        val actual = repository.getEnabledNewsletterHandlers()

        // THEN
        actual shouldBe enabledHandlers
    }
}
