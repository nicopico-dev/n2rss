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

import fr.nicopico.n2rss.fakes.FixedClock
import fr.nicopico.n2rss.fakes.NewsletterHandlerFake
import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.data.NewsletterRequestRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.NewsletterInfo
import fr.nicopico.n2rss.newsletter.models.NewsletterRequest
import fr.nicopico.n2rss.newsletter.models.Publication
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.kotlinx.datetime.shouldBeAfter
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class NewsletterServiceTest {

    private val newsletterHandlers = listOf(
        NewsletterHandlerFake(Newsletter("code1", "Name 1", "website1")),
        NewsletterHandlerFake(Newsletter("code2", "Name 2", "website2")),
        NewsletterHandlerFake(Newsletter("code3", "Name 3", "website3", "some notes")),
    )

    @MockK
    private lateinit var publicationRepository: PublicationRepository
    @MockK
    private lateinit var newsletterRequestRepository: NewsletterRequestRepository
    @MockK(relaxUnitFun = true)
    private lateinit var monitoringService: MonitoringService

    private val now by lazy { Clock.System.now() }

    private lateinit var newsletterService: NewsletterService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        newsletterService = NewsletterService(
            newsletterHandlers = newsletterHandlers,
            publicationRepository = publicationRepository,
            newsletterRequestRepository = newsletterRequestRepository,
            monitoringService = monitoringService,
            clock = FixedClock(now)
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
                notes = null,
            ),
            NewsletterInfo(
                code = "code2",
                title = "Name 2",
                websiteUrl = "website2",
                publicationCount = 3,
                startingDate = firstPublicationCode2,
                notes = null,
            ),
            NewsletterInfo(
                code = "code3",
                title = "Name 3",
                websiteUrl = "website3",
                publicationCount = 0,
                startingDate = null,
                notes = "some notes",
            ),
        )
    }

    @Test
    fun `new newsletterRequest are added to the database`() {
        // GIVEN
        val newsletterUrl = URL("https://www.nicopico.com")
        every { newsletterRequestRepository.getByNewsletterUrl(any()) } returns null
        every { newsletterRequestRepository.save(any()) } answers { firstArg() }

        // WHEN
        newsletterService.saveRequest(newsletterUrl)

        // THEN
        val slotNewsletterRequest = slot<NewsletterRequest>()
        verify {
            newsletterRequestRepository.getByNewsletterUrl(newsletterUrl)
            newsletterRequestRepository.save(capture(slotNewsletterRequest))
        }
        slotNewsletterRequest.captured should {
            it.newsletterUrl shouldBe newsletterUrl
            it.requestCount shouldBe 1
            it.firstRequestDate shouldBe it.lastRequestDate
        }

        verify { monitoringService.notifyRequest(newsletterUrl) }
    }

    @Test
    fun `variant of a newsletterRequest will be unified`() {
        // GIVEN
        @Suppress("HttpUrlsUsage")
        val newsletterUrl = URL("http://www.nicopico.com/test/")
        every { newsletterRequestRepository.getByNewsletterUrl(any()) } returns null
        every { newsletterRequestRepository.save(any()) } answers { firstArg() }
        val uniqueUrl = URL("https://www.nicopico.com")

        // WHEN
        newsletterService.saveRequest(newsletterUrl)

        // THEN
        val slotNewsletterRequest = slot<NewsletterRequest>()
        verify {
            newsletterRequestRepository.getByNewsletterUrl(uniqueUrl)
            newsletterRequestRepository.save(capture(slotNewsletterRequest))
        }
        slotNewsletterRequest.captured should {
            it.newsletterUrl shouldBe uniqueUrl
            it.requestCount shouldBe 1
            it.firstRequestDate shouldBe it.lastRequestDate
        }
        verify { monitoringService.notifyRequest(uniqueUrl) }
    }

    @Test
    fun `existing newsletterRequest are incremented in the database`() {
        // GIVEN
        val newsletterUrl = URL("https://www.nicopico.com")
        val existingRequest = NewsletterRequest(
            newsletterUrl = newsletterUrl,
            firstRequestDate = LocalDateTime(2020, 1, 1, 0, 0, 0),
            lastRequestDate = LocalDateTime(2020, 1, 10, 0, 0, 0),
            requestCount = 2,
        )

        every { newsletterRequestRepository.getByNewsletterUrl(any()) } returns existingRequest
        every { newsletterRequestRepository.save(any()) } answers { firstArg() }

        // WHEN
        newsletterService.saveRequest(newsletterUrl)

        // THEN
        val slotNewsletterRequest = slot<NewsletterRequest>()
        verify {
            newsletterRequestRepository.getByNewsletterUrl(newsletterUrl)
            newsletterRequestRepository.save(capture(slotNewsletterRequest))
        }
        slotNewsletterRequest.captured should {
            it.newsletterUrl shouldBe newsletterUrl
            it.requestCount shouldBe 3
            it.firstRequestDate shouldBe existingRequest.firstRequestDate
            it.lastRequestDate shouldBeAfter existingRequest.lastRequestDate
        }
    }
}
