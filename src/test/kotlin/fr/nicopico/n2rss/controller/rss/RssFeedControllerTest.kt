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

import fr.nicopico.n2rss.data.NewsletterRepository
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.models.Publication
import fr.nicopico.n2rss.service.NewsletterService
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifySequence
import jakarta.servlet.http.HttpServletResponse
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.net.URL

class RssFeedControllerTest {

    @MockK
    private lateinit var newsletterService: NewsletterService
    @MockK
    private lateinit var newsletterRepository: NewsletterRepository
    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK(relaxUnitFun = true)
    private lateinit var rssOutputWriter: RssOutputWriter

    private lateinit var rssFeedController: RssFeedController

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        rssFeedController = RssFeedController(
            newsletterService,
            newsletterRepository,
            publicationRepository,
            rssOutputWriter,
        )
    }

    @Test
    fun `getRssFeeds should returns info on all the current feeds`() {
        // GIVEN
        every { newsletterService.getNewslettersInfo() } returns List(2) {
            NewsletterInfo(
                code = "newsletter_$it",
                title = "Newsletter$it",
                websiteUrl = "https://website$it.com",
                publicationCount = (it + 1) * 2L,
                startingDate = null,
            )
        }

        // WHEN
        val result = rssFeedController.getRssFeeds()

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

        val expectedNewsletter = Newsletter("test newsletter", "https://test.com")
        val expectedPublication = Publication(
            title = "test publication",
            date = LocalDate.fromEpochDays(321),
            newsletter = expectedNewsletter,
            articles = listOf(
                Article("Article 1", URL("http://article1.com"), "Some description")
            )
        )
        every { newsletterRepository.findNewsletterByCode("test") } returns expectedNewsletter
        every { publicationRepository.findByNewsletter(expectedNewsletter, any()) } returns
                PageImpl(listOf(expectedPublication))

        // WHEN
        rssFeedController.getFeed("test", 0, 2, mockResponse)

        // THEN
        verifySequence {
            newsletterRepository.findNewsletterByCode("test")
            publicationRepository.findByNewsletter(
                expectedNewsletter,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "date"))
            )
            rssOutputWriter.write(any(), any())
        }
    }

    @Test
    fun `getFeed should return a 404 error if the feed does not exist`() {
        // GIVEN
        val mockResponse = mockk<HttpServletResponse>(relaxed = true)
        every { newsletterRepository.findNewsletterByCode(any()) } returns null

        // WHEN-THEN
        rssFeedController.getFeed("test", 0, 2, mockResponse)

        // THEN
        verifySequence {
            newsletterRepository.findNewsletterByCode("test")
            mockResponse.sendError(404)
        }
    }
}
