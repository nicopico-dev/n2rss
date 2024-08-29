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
package fr.nicopico.n2rss.newsletter.service

import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verifySequence
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.net.URL

class RssServiceTest {

    private lateinit var rssService: RssService

    @MockK
    private lateinit var newsletterRepository: NewsletterRepository
    @MockK
    private lateinit var publicationRepository: PublicationRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        rssService = RssService(
            newsletterRepository = newsletterRepository,
            publicationRepository = publicationRepository
        )
    }

    @Test
    fun `getFeed should return the content of the feed`() {
        // GIVEN
        val expectedNewsletter = Newsletter(
            code = "test",
            name = "test newsletter",
            websiteUrl = "https://test.com",
            feedTitle = "This is a test FEED",
        )
        val expectedPublication = Publication(
            title = "test publication",
            date = LocalDate.fromEpochDays(321),
            newsletter = expectedNewsletter,
            articles = listOf(
                Article("Article 1", URL("https://article1.com"), "Some description")
            )
        )
        every { newsletterRepository.findNewsletterByCode("test") } returns expectedNewsletter
        every { publicationRepository.findByNewsletter(expectedNewsletter, any()) } returns
            PageImpl(listOf(expectedPublication))

        // WHEN
        val feed = rssService.getFeed("test", 0, 2)

        // THEN
        feed.title shouldBe "This is a test FEED"
        feed.entries shouldHaveSize expectedPublication.articles.size
        verifySequence {
            newsletterRepository.findNewsletterByCode("test")
            publicationRepository.findByNewsletter(
                expectedNewsletter,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "date"))
            )
        }
    }

    @Test
    fun `getFeed should default to newsletter name if no feedTitle are provided`() {
        // GIVEN
        val expectedNewsletter = Newsletter(
            code = "test",
            name = "test newsletter",
            websiteUrl = "https://test.com",
        )
        val expectedPublication = Publication(
            title = "test publication",
            date = LocalDate.fromEpochDays(321),
            newsletter = expectedNewsletter,
            articles = listOf(
                Article("Article 1", URL("https://article1.com"), "Some description")
            )
        )
        every { newsletterRepository.findNewsletterByCode("test") } returns expectedNewsletter
        every { publicationRepository.findByNewsletter(expectedNewsletter, any()) } returns
            PageImpl(listOf(expectedPublication))

        // WHEN
        val feed = rssService.getFeed("test", 0, 2)

        // THEN
        feed.title shouldBe "test newsletter"
    }

    @Test
    fun `getFeed should throw an error if the feed does not exist`() {
        // GIVEN
        every { newsletterRepository.findNewsletterByCode(any()) } returns null

        // WHEN-THEN
        shouldThrow<NoSuchElementException> {
            rssService.getFeed("test", 0, 2)
        }

        // THEN
        verifySequence {
            newsletterRepository.findNewsletterByCode("test")
        }
    }
}
