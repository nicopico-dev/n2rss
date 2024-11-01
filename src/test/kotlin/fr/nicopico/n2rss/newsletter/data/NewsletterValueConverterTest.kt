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
import fr.nicopico.n2rss.newsletter.data.legacy.NewsletterValueConverter
import fr.nicopico.n2rss.newsletter.models.Newsletter
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

class NewsletterValueConverterTest {

    @Test
    fun `a newsletter should be serialized to its code`() {
        // GIVEN
        val converter = NewsletterValueConverter(emptyList())

        // WHEN
        val newsletter = Newsletter("code", "name", "websiteUrl")
        val result = converter.write(newsletter, mockk())

        // THEN
        result shouldBe "code"
    }

    @Test
    fun `a newsletter code should be deserialized to the corresponding newsletter`() {
        // GIVEN
        val newsletter = Newsletter("code", "name", "websiteUrl")
        val converter = NewsletterValueConverter(
            listOf(
                NewsletterHandlerFake("foo"),
                NewsletterHandlerFake(newsletter),
                NewsletterHandlerFake("bar"),
            )
        )

        // WHEN
        val result = converter.read("code", mockk())

        // THEN
        result shouldBe newsletter
    }

    @Test
    fun `a newsletter code should be deserialized to null if no match is found`() {
        // GIVEN
        val converter = NewsletterValueConverter(
            listOf(
                NewsletterHandlerFake("foo"),
                NewsletterHandlerFake("bar"),
            )
        )

        // WHEN
        val result = converter.read("code", mockk())

        // THEN
        result shouldBe null
    }
}
