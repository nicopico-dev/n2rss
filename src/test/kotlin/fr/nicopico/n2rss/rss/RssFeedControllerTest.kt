package fr.nicopico.n2rss.rss

import fr.nicopico.n2rss.data.NewsletterRepository
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.Publication
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
    private lateinit var newsletterRepository: NewsletterRepository
    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK(relaxUnitFun = true)
    private lateinit var rssOutputWriter: RssOutputWriter

    private lateinit var rssFeedController: RssFeedController

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val newsletterHandlers = List(2) {
            mockk<NewsletterHandler> {
                val newsletter = Newsletter("Newsletter$it", "https://website$it.com")
                every { this@mockk.newsletter } returns newsletter
            }
        }

        rssFeedController = RssFeedController(
            newsletterHandlers,
            newsletterRepository,
            publicationRepository,
            rssOutputWriter,
        )
    }

    @Test
    fun `getRssFeeds should returns info on all the current feeds`() {
        // GIVEN
        every { publicationRepository.countPublicationsByNewsletter(any()) } returns 5 andThen 8

        // WHEN
        val result = rssFeedController.getRssFeeds()

        // THEN
        result shouldHaveSize 2
        assertSoftly(result[0]) {
            title shouldBe "Newsletter0"
            publicationCount shouldBe 5
        }
        assertSoftly(result[1]) {
            title shouldBe "Newsletter1"
            publicationCount shouldBe 8
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
        rssFeedController.getFeed("test", mockResponse, 0, 2)

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
        rssFeedController.getFeed("test", mockResponse, 0, 2)

        // THEN
        verifySequence {
            newsletterRepository.findNewsletterByCode("test")
            mockResponse.sendError(404)
        }
    }
}
