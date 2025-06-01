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

package fr.nicopico.n2rss.controller.rss

import com.rometools.rome.feed.synd.SyndFeedImpl
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RssOutputWriterTest {

    @MockK(relaxed = true)
    private lateinit var response: HttpServletResponse

    private lateinit var rssOutputWriter: RssOutputWriter

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        rssOutputWriter = RssOutputWriter()
    }

    @Test
    fun `RssOutputWriter should be able to write RSS feed to an HTTP response`() {
        // GIVEN
        val feed = SyndFeedImpl().apply {
            feedType = "rss_2.0"
            title = "title"
            link = "link"
            description = "description"
        }

        // WHEN - THEN
        shouldNotThrowAny {
            rssOutputWriter.write(feed, response)
        }
    }
}
