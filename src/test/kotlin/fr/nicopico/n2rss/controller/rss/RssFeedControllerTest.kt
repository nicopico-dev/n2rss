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

package fr.nicopico.n2rss.controller.rss

import com.rometools.rome.feed.synd.SyndFeed
import fr.nicopico.n2rss.newsletter.models.NewsletterInfo
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.RssService
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifySequence
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RssFeedControllerTest {

    private lateinit var rssFeedController: RssFeedController

    @MockK
    private lateinit var newsletterService: NewsletterService
    @MockK
    private lateinit var rssService: RssService
    @MockK(relaxUnitFun = true)
    private lateinit var rssOutputWriter: RssOutputWriter

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        rssFeedController = RssFeedController(
            newsletterService,
            rssService,
            rssOutputWriter,
        )
    }

    @Test
    fun `getRssFeeds should returns info on all the current feeds`() {
        // GIVEN
        every { newsletterService.getEnabledNewslettersInfo() } returns List(2) {
            NewsletterInfo(
                code = "newsletter_$it",
                title = "Newsletter$it",
                websiteUrl = "https://website$it.com",
                publicationCount = (it + 1) * 2L,
                startingDate = null,
                notes = null,
            )
        }

        // WHEN
        val result = rssFeedController.getRssFeeds("userAgent")

        // THEN
        result shouldHaveSize 2
        assertSoftly(result[0]) {
            title shouldBe "Newsletter0"
            publicationCount shouldBe 2
        }
        assertSoftly(result[1]) {
            title shouldBe "Newsletter1"
            publicationCount shouldBe 4
        }
    }

    @Test
    fun `getFeed should return an RSS feed`() {
        // GIVEN
        val mockResponse = mockk<HttpServletResponse>(relaxed = true)

        val feed = mockk<SyndFeed>()
        every { rssService.getFeed(any(), any(), any()) } returns feed

        // WHEN
        rssFeedController.getFeed("test", 0, 2, "userAgent", mockResponse)

        // THEN
        verifySequence {
            rssService.getFeed("test", 0, 2)
            rssOutputWriter.write(feed, any())
        }
    }

    @Test
    fun `getFeed should return an RSS feed for feed inside a folder`() {
        // GIVEN
        val mockResponse = mockk<HttpServletResponse>(relaxed = true)

        val feed = mockk<SyndFeed>()
        every { rssService.getFeed(any(), any(), any()) } returns feed

        // WHEN
        rssFeedController.getFeed("folder", "test", 0, 2, "userAgent", mockResponse)

        // THEN
        verifySequence {
            rssService.getFeed("folder/test", 0, 2)
            rssOutputWriter.write(feed, any())
        }
    }

    @Test
    fun `getFeed should return a 404 error if the feed does not exist`() {
        // GIVEN
        val mockResponse = mockk<HttpServletResponse>(relaxed = true)
        every { rssService.getFeed(any(), any(), any()) } throws NoSuchElementException()

        // WHEN-THEN
        rssFeedController.getFeed("test", 0, 2, "userAgent", mockResponse)

        // THEN
        verifySequence {
            rssService.getFeed("test", 0, 2)
            mockResponse.sendError(404)
        }
    }
}
