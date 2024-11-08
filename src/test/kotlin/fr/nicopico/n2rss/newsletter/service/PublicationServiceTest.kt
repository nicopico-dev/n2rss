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
import fr.nicopico.n2rss.newsletter.data.legacy.LegacyPublicationRepository
import fr.nicopico.n2rss.newsletter.data.legacy.PublicationDocument
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
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
import java.util.UUID

@ExtendWith(MockKExtension::class)
class PublicationServiceTest {

    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK
    private lateinit var legacyPublicationRepository: LegacyPublicationRepository
    @MockK
    private lateinit var newsletterRepository: NewsletterRepository

    private fun createPublicationService(persistenceMode: PersistenceMode): PublicationService {
        return PublicationService(
            publicationRepository = publicationRepository,
            legacyPublicationRepository = legacyPublicationRepository,
            newsletterRepository = newsletterRepository,
            persistenceMode = persistenceMode,
        )
    }

    private fun createStubNewsletter() = Newsletter(
        code = "test",
        name = "Test Newsletter",
        websiteUrl = "https://test.com"
    )

    private fun createStubPublication(
        newsletter: Newsletter,
        title: String = "Title 1",
        articleCount: Int = 1,
    ) = Publication(
        title = title,
        date = LocalDate.fromEpochDays(321),
        newsletter = newsletter,
        articles = List(articleCount) { mockk<Article>() }
    )

    private fun createStubPublicationDocument(
        newsletter: Newsletter,
    ) = PublicationDocument(
        id = UUID.randomUUID(),
        title = "Title 1",
        date = LocalDate.fromEpochDays(321),
        newsletter = newsletter,
        articles = listOf(mockk<Article>())
    )

    @Nested
    inner class LegacyMode {

        private lateinit var publicationService: PublicationService

        @BeforeEach
        fun setUp() {
            publicationService = createPublicationService(PersistenceMode.LEGACY)
        }

        @Test
        fun `should get publications by newsletter`() {
            // GIVEN
            val newsletter = createStubNewsletter()
            val pageable = PageRequest.of(0, 10)
            val publications = listOf(createStubPublicationDocument(newsletter))
            val publicationPage: Page<PublicationDocument> = PageImpl(publications)
            every { legacyPublicationRepository.findByNewsletter(newsletter, pageable) } returns publicationPage

            // WHEN
            val result = publicationService.getPublications(newsletter, pageable)

            // THEN
            verify { legacyPublicationRepository.findByNewsletter(newsletter, pageable) }
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

            every { legacyPublicationRepository.saveAll(any<List<PublicationDocument>>()) } answers { firstArg() }

            // WHEN
            publicationService.savePublications(publications)

            // THEN
            val slot = slot<List<PublicationDocument>>()
            verify { legacyPublicationRepository.saveAll(capture(slot)) }
            slot.captured should {
                it shouldHaveSize 2
                it[0].title shouldBe "Title 1"
                it[1].title shouldBe "Title 3"
            }
        }

        @Test
        fun `should get publications count by newsletter`() {
            // GIVEN
            val newsletter = createStubNewsletter()
            every { legacyPublicationRepository.countPublicationsByNewsletter(newsletter) } returns 10L

            // WHEN
            val result = publicationService.getPublicationsCount(newsletter)

            // THEN
            verify { legacyPublicationRepository.countPublicationsByNewsletter(newsletter) }
            result shouldBe 10L
        }

        @Test
        fun `should get latest publication date by newsletter`() {
            // GIVEN
            val newsletter = createStubNewsletter()
            val latestPublication = createStubPublicationDocument(newsletter)
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) } returns latestPublication

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe latestPublication.date
        }

        @Test
        fun `should return null when no publications are available`() {
            // GIVEN
            val newsletter = createStubNewsletter()
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) } returns null

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe null
        }
    }
}
