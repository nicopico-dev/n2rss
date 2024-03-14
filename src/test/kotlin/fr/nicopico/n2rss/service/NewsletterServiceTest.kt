package fr.nicopico.n2rss.service

import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.fakes.NewsletterHandlerFake
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.models.Publication
import io.kotest.matchers.collections.shouldContainOnly
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NewsletterServiceTest {

    private val newsletterHandlers = listOf(
        NewsletterHandlerFake(Newsletter("code1", "Name 1", "website1")),
        NewsletterHandlerFake(Newsletter("code2", "Name 2", "website2")),
        NewsletterHandlerFake(Newsletter("code3", "Name 3", "website3")),
    )

    @MockK
    private lateinit var publicationRepository: PublicationRepository

    private lateinit var newsletterService: NewsletterService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        newsletterService = NewsletterService(
            newsletterHandlers = newsletterHandlers,
            publicationRepository = publicationRepository,
        )
    }

    @Test
    fun `should give info about all supported newsletters`() {
        // GIVEN
        val firstPublicationCode1 = LocalDate(2022, 3, 21)
        val firstPublicationCode2 = LocalDate(2023, 7, 8)
        every { publicationRepository.countPublicationsByNewsletter(any()) } answers {
            when (firstArg<Newsletter>().code) {
                "code1" -> 32
                "code2" -> 3
                else -> 0
            }
        }
        every { publicationRepository.findFirstByNewsletterOrderByDateAsc(any()) } answers {
            val newsletter = firstArg<Newsletter>()
            when (newsletter.code) {
                "code1" -> Publication(
                    title = "Some title1",
                    date = firstPublicationCode1,
                    newsletter = newsletter,
                    articles = emptyList()
                )

                "code2" -> Publication(
                    title = "Some title 2",
                    date = firstPublicationCode2,
                    newsletter = newsletter,
                    articles = emptyList()
                )

                else -> null
            }
        }

        // WHEN
        val result = newsletterService.getNewslettersInfo()

        // WHEN
        result shouldContainOnly listOf(
            NewsletterInfo(
                code = "code1",
                title = "Name 1",
                websiteUrl = "website1",
                publicationCount = 32,
                startingDate = firstPublicationCode1,
            ),
            NewsletterInfo(
                code = "code2",
                title = "Name 2",
                websiteUrl = "website2",
                publicationCount = 3,
                startingDate = firstPublicationCode2,
            ),
            NewsletterInfo(
                code = "code3",
                title = "Name 3",
                websiteUrl = "website3",
                publicationCount = 0,
                startingDate = null,
            ),
        )
    }
}
