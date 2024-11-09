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
import fr.nicopico.n2rss.newsletter.data.legacy.LegacyPublicationRepository
import fr.nicopico.n2rss.newsletter.data.legacy.PublicationDocument
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
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.domain.Sort.Order
import java.net.URL
import java.util.UUID
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class PublicationServiceTest {

    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK
    private lateinit var legacyPublicationRepository: LegacyPublicationRepository
    @MockK
    private lateinit var newsletterRepository: NewsletterRepository

    //region Helpers
    private fun createPublicationService(persistenceMode: PersistenceMode): PublicationService {
        return PublicationService(
            publicationRepository = publicationRepository,
            legacyPublicationRepository = legacyPublicationRepository,
            newsletterRepository = newsletterRepository,
            persistenceMode = persistenceMode,
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

    private fun createStubPublicationDocument(
        newsletter: Newsletter,
        title: String = "Title 1",
        publicationDate: LocalDate = LocalDate.now(),
    ) = PublicationDocument(
        id = UUID.randomUUID(),
        title = title,
        date = publicationDate,
        newsletter = newsletter,
        articles = listOf(mockk<Article>())
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

    @Nested
    inner class DefaultMode {

        private lateinit var publicationService: PublicationService

        @BeforeEach
        fun setUp() {
            publicationService = createPublicationService(PersistenceMode.DEFAULT)
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
            val result = publicationService.getLatestPublicationDate(newsletter)

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
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            result shouldBe null
        }
    }

    @Nested
    inner class MigrationMode {

        private lateinit var publicationService: PublicationService

        @BeforeEach
        fun setUp() {
            publicationService = createPublicationService(PersistenceMode.MIGRATION)
        }

        @Test
        fun `should get publications by newsletter`() {
            // GIVEN
            val newsletterCode = "test"
            val newsletter = createStubNewsletter(newsletterCode)
            every { newsletterRepository.findNewsletterByCode(any()) } returns newsletter

            val pageable = PageRequest.of(0, 4)
                .withSort(
                    Sort.by(
                        Order(DESC, "date"),
                        Order(ASC, "title"),
                        Order(ASC, "newsletter")
                    )
                )

            val latestPublicationDate = LocalDate.now()
            val publications = listOf(
                createStubPublicationEntity(newsletter, title = "Title 1A", publicationDate = latestPublicationDate),
                createStubPublicationEntity(
                    newsletter,
                    title = "Title 2A",
                    publicationDate = latestPublicationDate - DatePeriod(days = 1)
                ),
                createStubPublicationEntity(
                    newsletter,
                    title = "Title 3A",
                    publicationDate = latestPublicationDate - DatePeriod(days = 2)
                ),
                createStubPublicationEntity(
                    newsletter,
                    title = "Title 4A",
                    publicationDate = latestPublicationDate - DatePeriod(days = 3)
                ),
            )
            every { publicationRepository.findByNewsletterCode(any(), any()) } returns PageImpl(publications)

            val legacyPublications = listOf(
                createStubPublicationDocument(
                    newsletter,
                    title = "Title 1B",
                    publicationDate = latestPublicationDate - DatePeriod(days = 2)
                ),
                createStubPublicationDocument(
                    newsletter,
                    title = "Title 2B",
                    publicationDate = latestPublicationDate - DatePeriod(days = 3)
                ),
                createStubPublicationDocument(
                    newsletter,
                    title = "Title 3B",
                    publicationDate = latestPublicationDate - DatePeriod(days = 4)
                ),
                createStubPublicationDocument(
                    newsletter,
                    title = "Title 4B",
                    publicationDate = latestPublicationDate - DatePeriod(days = 5)
                ),
            )
            every { legacyPublicationRepository.findByNewsletter(any(), any()) } returns PageImpl(legacyPublications)

            // WHEN
            val result = publicationService.getPublications(newsletter, pageable)

            // THEN
            verify { newsletterRepository.findNewsletterByCode(newsletterCode) }
            verify { publicationRepository.findByNewsletterCode(newsletterCode, pageable) }
            assertSoftly {
                result.content.size shouldBe 4
                result.content.map { it.title } shouldBe listOf("Title 1A", "Title 2A", "Title 1B", "Title 3A")
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
            every { publicationRepository.countPublicationsByNewsletterCode(any()) } returns 4L
            every { legacyPublicationRepository.countPublicationsByNewsletter(any()) } returns 7L

            // WHEN
            val result = publicationService.getPublicationsCount(newsletter)

            // THEN
            verify { publicationRepository.countPublicationsByNewsletterCode(newsletterCode) }
            verify { legacyPublicationRepository.countPublicationsByNewsletter(newsletter) }
            result shouldBe 11L
        }

        @Test
        fun `should get latest publication date by newsletter - 1`() {
            // GIVEN
            val newsletterCode = "primarily"
            val newsletter = createStubNewsletter(newsletterCode)

            val latestPublicationDate = LocalDate.now()
            val latestPublication = createStubPublicationEntity(
                newsletter = newsletter, publicationDate = latestPublicationDate,
            )
            val latestLegacyPublication = createStubPublicationDocument(
                newsletter = newsletter, publicationDate = latestPublicationDate - DatePeriod(days = 1),
            )

            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns latestPublication
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(any()) } returns latestLegacyPublication

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe latestPublicationDate
        }

        @Test
        fun `should get latest publication date by newsletter - 2`() {
            // GIVEN
            val newsletterCode = "primarily"
            val newsletter = createStubNewsletter(newsletterCode)

            val latestPublicationDate = LocalDate.now()
            val latestPublication = createStubPublicationEntity(
                newsletter = newsletter, publicationDate = latestPublicationDate,
            )

            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns latestPublication
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(any()) } returns null

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe latestPublicationDate
        }

        @Test
        fun `should get latest publication date by newsletter - 3`() {
            // GIVEN
            val newsletterCode = "primarily"
            val newsletter = createStubNewsletter(newsletterCode)

            val latestPublicationDate = LocalDate.now()
            val latestLegacyPublication = createStubPublicationDocument(
                newsletter = newsletter, publicationDate = latestPublicationDate,
            )

            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns null
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(any()) } returns latestLegacyPublication

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe latestPublicationDate
        }

        @Test
        fun `should get latest publication date by newsletter - 4`() {
            // GIVEN
            val newsletterCode = "primarily"
            val newsletter = createStubNewsletter(newsletterCode)

            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns null
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(any()) } returns null

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe null
        }

        @Test
        fun `should return null when no publications are available`() {
            // GIVEN
            val newsletterCode = "codes"
            val newsletter = createStubNewsletter(newsletterCode)
            every { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(any()) } returns null
            every { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(any()) } returns null

            // WHEN
            val result = publicationService.getLatestPublicationDate(newsletter)

            // THEN
            verify { publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletterCode) }
            verify { legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
            result shouldBe null
        }
    }
}
