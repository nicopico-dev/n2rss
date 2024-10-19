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

import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
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

    private lateinit var publicationService: PublicationService

    @BeforeEach
    fun setUp() {
        publicationService = PublicationService(publicationRepository)
    }

    private fun createStubNewsletter() = Newsletter(
        code = "test",
        name = "Test Newsletter",
        websiteUrl = "https://test.com"
    )

    private fun createStubPublication(newsletter: Newsletter) = Publication(
        id = UUID.randomUUID(),
        title = "Title 1",
        date = LocalDate.fromEpochDays(321),
        newsletter = newsletter,
        articles = emptyList()
    )

    @Test
    fun `should get publications by newsletter`() {
        // GIVEN
        val newsletter = createStubNewsletter()
        val pageable = PageRequest.of(0, 10)
        val publications = listOf(
            createStubPublication(newsletter)
        )
        val publicationPage: Page<Publication> = PageImpl(publications)
        every { publicationRepository.findByNewsletter(newsletter, pageable) } returns publicationPage

        // WHEN
        val result = publicationService.getPublications(newsletter, pageable)

        // THEN
        verify { publicationRepository.findByNewsletter(newsletter, pageable) }
        assertSoftly {
            result.content.size shouldBe 1
            result.content[0].title shouldBe "Title 1"
        }
    }

    @Test
    fun `should save publications if the list is not empty`() {
        // GIVEN
        val newsletter = createStubNewsletter()
        val publications = listOf(
            createStubPublication(newsletter)
        )
        every { publicationRepository.saveAll(publications) } returns publications

        // WHEN
        publicationService.savePublications(publications)

        // THEN
        verify { publicationRepository.saveAll(publications) }
    }

    @Test
    fun `should not save publications if the list is empty`() {
        // GIVEN
        val publications = emptyList<Publication>()

        // WHEN
        publicationService.savePublications(publications)

        // THEN
        verify(exactly = 0) { publicationRepository.saveAll(publications) }
    }

    @Test
    fun `should get publications count by newsletter`() {
        // GIVEN
        val newsletter = createStubNewsletter()
        every { publicationRepository.countPublicationsByNewsletter(newsletter) } returns 10L

        // WHEN
        val result = publicationService.getPublicationsCount(newsletter)

        // THEN
        verify { publicationRepository.countPublicationsByNewsletter(newsletter) }
        result shouldBe 10L
    }

    @Test
    fun `should get latest publication date by newsletter`() {
        // GIVEN
        val newsletter = createStubNewsletter()
        val latestPublication = createStubPublication(newsletter)
        every { publicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) } returns latestPublication

        // WHEN
        val result = publicationService.getLatestPublicationDate(newsletter)

        // THEN
        verify { publicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
        result shouldBe latestPublication.date
    }

    @Test
    fun `should return null when no publications are available`() {
        // GIVEN
        val newsletter = createStubNewsletter()
        every { publicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) } returns null

        // WHEN
        val result = publicationService.getLatestPublicationDate(newsletter)

        // THEN
        verify { publicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter) }
        result shouldBe null
    }
}
