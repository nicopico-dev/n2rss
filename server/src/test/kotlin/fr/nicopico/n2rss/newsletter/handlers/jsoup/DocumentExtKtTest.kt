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

package fr.nicopico.n2rss.newsletter.handlers.jsoup

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Test

class DocumentExtKtTest {

    @Test
    fun `indexOf should return the index of the element`() {
        // GIVEN
        val document = createDocument(HTML_SAMPLE)
        val element = document.selectFirst("span.bold")!!

        // WHEN
        val actual = document.indexOf(element)

        // THEN
        actual shouldBe 8
    }

    @Test
    fun `indexOf should return -1 if the element does not exist in the document`() {
        // GIVEN
        val document = createDocument(HTML_SAMPLE)
        val element = Element("a")

        // WHEN
        val actual = document.indexOf(element)

        // THEN
        actual shouldBe -1
    }

    @Test
    fun `select should return the first element matching the selector after the provided index`() {
        // GIVEN
        val document = createDocument(HTML_SAMPLE)

        // WHEN
        val element = document.select("span", 8)

        // THEN
        withClue("element") {
            element?.text() shouldBe "Else"
        }
    }

    @Test
    fun `select should return null if no elements match the selector`() {
        // GIVEN
        val document = createDocument(HTML_SAMPLE)

        // WHEN
        val element = document.select("a", 8)

        // THEN
        withClue("element") {
            element shouldBe null
        }
    }

    @Test
    fun `select should throw if index is less than 0`() {
        // GIVEN
        val document = createDocument(HTML_SAMPLE)

        // WHEN - THEN
        shouldThrow<IllegalArgumentException> {
            document.select("span", -1)
        }
    }

    @Test
    fun `select should throw if index is greater than the last index of the document`() {
        // GIVEN
        val document = createDocument(HTML_SAMPLE)

        // WHEN - THEN
        shouldThrow<IllegalArgumentException> {
            document.select("span", 11)
        }
    }

    companion object {
        private const val HTML_SAMPLE = """
            <table>
                <tr>
                    <td>
                        <span class="bold">Something</span>
                    </td>
                    <td>
                        <span>Else</span>
                    </td>
                </tr>
            </table>
        """

        private fun createDocument(html: String): Document {
            return Jsoup.parseBodyFragment(html)
        }
    }
}
