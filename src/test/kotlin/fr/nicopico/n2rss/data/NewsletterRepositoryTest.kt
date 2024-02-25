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

package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NewsletterRepositoryTest {

    @MockK
    private lateinit var handler: NewsletterHandler

    private lateinit var repository: NewsletterRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        repository = NewsletterRepository(listOf(handler))
    }

    @Test
    fun `should return the newsletter for a given code`() {
        // GIVEN
        val code = "code"
        every { handler.newsletter.code } returns code

        // WHEN
        val actual = repository.findNewsletterByCode(code)

        // THEN
        actual shouldBe handler.newsletter
    }

    @Test
    fun `should return null if no newsletter correspond to the provided code`() {
        // GIVEN
        val code = "code"
        every { handler.newsletter.code } returns "foo"

        // WHEN
        val actual = repository.findNewsletterByCode(code)

        // THEN
        actual shouldBe null
    }
}
