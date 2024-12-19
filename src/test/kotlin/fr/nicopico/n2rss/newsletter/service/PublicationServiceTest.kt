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

import fr.nicopico.n2rss.config.PersistenceMode
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.data.entity.ArticleEntity
import fr.nicopico.n2rss.newsletter.data.entity.PublicationEntity
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import fr.nicopico.n2rss.utils.now
import fr.nicopico.n2rss.utils.toLegacyDate
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.net.URL
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class PublicationServiceTest {

    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK
    private lateinit var newsletterRepository: NewsletterRepository

    //region Helpers
    private fun createPublicationService(): PublicationService {
        return PublicationService(
            publicationRepository = publicationRepository,
            newsletterRepository = newsletterRepository,
            persistenceMode = PersistenceMode.DEFAULT,
        )
    }

    private fun createStubNewsletter(code: String = "test") = Newsletter(
        code = code,
        name = "Test Newsletter",
        websiteUrl = "https://test.com"
    )

    private fun createStubPublication(
        newsletter: Newsletter,
        title: String = "Title 1",
        articleCount: Int = 1,
        publicationDate: LocalDate = LocalDate.now(),
    ) = Publication(
        title = title,
        date = publicationDate,
        newsletter = newsletter,
        articles = List(articleCount) { index ->
            Article(
                title = "Article Title $index",
                link = URL("https://example.com/article-$index"),
                description = "This is a description for a random article."
            )
        }
    )

    private fun createStubPublicationEntity(
        newsletter: Newsletter,
        title: String = "Title 1",
        publicationDate: LocalDate = LocalDate.now(),
    ): PublicationEntity {
        val publicationEntity = PublicationEntity(
            id = Random.nextLong(),
            title = title,
            date = publicationDate.toLegacyDate(),
            newsletterCode = newsletter.code,
        ).also {
            it.articles = listOf(
                ArticleEntity(
                    id = Random.nextLong(),
                    title = "Random Title ${Random.nextInt()}",
                    link = "https://example.com/article-${Random.nextInt()}",
                    description = "This is a description for a random article.",
                    publication = it,
                )
            )
        }
        return publicationEntity
    }
    //endregion

    @Nested
    inner class DefaultMode {

        private lateinit var publicationService: PublicationService

        @BeforeEach
        fun setUp() {
            publicationService = createPublicationService()
        }

        @Test
        fun `should get publications by newsletter`() {
            // GIVEN
            val newsletterCode = "test"
            val newsletter = createStubNewsletter(newsletterCode)
            val pageable = PageRequest.of(0, 10)
            val publications = listOf(createStubPublicationEntity(newsletter))
            val publicationPage: Page<PublicationEntity> = PageImpl(publications)
            every { newsletterRepository.findNewsletterByCode(any()) } returns newsletter
            every { publicationRepository.findByNewsletterCode(any(), any()) } returns publicationPage

            // WHEN
            val result = publicationService.getPublications(newsletter, pageable)

            // THEN
            verify { newsletterRepository.findNewsletterByCode(newsletterCode) }
            verify { publicationRepository.findByNewsletterCode(newsletterCode, pageable) }
            assertSoftly {
                result.content.size shouldBe 1
                result.content[0].title shouldBe "Title 1"
            }
        }

        @Test
        fun `should only save publications containing articles`() {
            // GIVEN
            val newsletter = createStubNewsletter()
            val publications = listOf(
                createStubPublication(newsletter, "Title 1"),
                createStubPublication(newsletter, "Title 2", articleCount = 0),
                createStubPublication(newsletter, "Title 3"),
            )

            every { publicationRepository.saveAll(any<List<PublicationEntity>>()) } answers { firstArg() }

            // WHEN
            publicationService.savePublications(publications)

            // THEN
            val slot = slot<List<PublicationEntity>>()
            verify { publicationRepository.saveAll(capture(slot)) }
            slot.captured should {
                it shouldHaveSize 2
                it[0].title shouldBe "Title 1"
                it[1].title shouldBe "Title 3"
            }
        }

        @Test
        fun `should get publications count by newsletter`() {
            // GIVEN
            val newsletterCode = "delivering"
            val newsletter = createStubNewsletter(newsletterCode)
            every { publicationRepository.countPublicationsByNewsletterCode(any()) } returns 10L

            // WHEN
            val result = publicationService.getPublicationsCount(newsletter)

            // THEN
            verify { publicationRepository.countPublicationsByNewsletterCode(newsletterCode) }
            result shouldBe 10L
        }

        @Test
        fun `should get latest publication date by newsletter`() {
            // GIVEN
            val newsletterCode = "primarily"
            val newsletter = createStubNewsletter(newsletterCode)
            val latestPublication = createStubPublicationEntity(newsletter)
            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns latestPublication

            // WHEN
            val result = publicationService.getOldestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            @Suppress("DEPRECATION")
            result shouldBe LocalDate(
                year = latestPublication.date.year + 1900,
                monthNumber = latestPublication.date.month + 1,
                dayOfMonth = latestPublication.date.date,
            )
        }

        @Test
        fun `should return null when no publications are available`() {
            // GIVEN
            val newsletterCode = "codes"
            val newsletter = createStubNewsletter(newsletterCode)
            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns null

            // WHEN
            val result = publicationService.getOldestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            result shouldBe null
        }
    }
}
