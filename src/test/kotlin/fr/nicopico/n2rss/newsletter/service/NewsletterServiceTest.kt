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

import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.NewsletterInfo
import io.kotest.matchers.collections.shouldContainOnly
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class NewsletterServiceTest {

    private val newsletters = listOf(
        Newsletter("code1", "Name 1", "website1"),
        Newsletter("code2", "Name 2", "website2"),
        Newsletter("code3", "Name 3", "website3", "some notes"),
    )

    @MockK
    private lateinit var newsletterRepository: NewsletterRepository
    @MockK
    private lateinit var publicationService: PublicationService
    @MockK(relaxUnitFun = true)
    private lateinit var monitoringService: MonitoringService

    private lateinit var newsletterService: NewsletterService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        newsletterService = NewsletterService(
            newsletterRepository = newsletterRepository,
            publicationService = publicationService,
            monitoringService = monitoringService
        )
    }

    @Test
    fun `should give info about all supported newsletters`() {
        // GIVEN
        val firstPublicationCode1 = LocalDate(2022, 3, 21)
        val firstPublicationCode2 = LocalDate(2023, 7, 8)
        every { publicationService.getPublicationsCount(any()) } answers {
            when (firstArg<Newsletter>().code) {
                "code1" -> 32
                "code2" -> 3
                else -> 0
            }
        }
        every { publicationService.getLatestPublicationDate(any()) } answers {
            val newsletter = firstArg<Newsletter>()
            when (newsletter.code) {
                "code1" -> firstPublicationCode1
                "code2" -> firstPublicationCode2
                else -> null
            }
        }
        every { newsletterRepository.getEnabledNewsletters() } returns newsletters

        // WHEN
        val result = newsletterService.getEnabledNewslettersInfo()

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

        // WHEN
        newsletterService.saveNewsletterRequest(newsletterUrl)

        // THEN
        verify { monitoringService.notifyRequest(newsletterUrl) }
    }

    @Test
    fun `variant of a newsletterRequest will be unified`() {
        // GIVEN
        @Suppress("HttpUrlsUsage")
        val newsletterUrl = URL("http://www.nicopico.com/test/")
        val uniqueUrl = URL("https://www.nicopico.com")

        // WHEN
        newsletterService.saveNewsletterRequest(newsletterUrl)

        // THEN
        verify { monitoringService.notifyRequest(uniqueUrl) }
    }

    @Test
    fun `existing newsletterRequest are incremented in the database`() {
        // GIVEN
        val newsletterUrl = URL("https://www.nicopico.com")

        // WHEN
        newsletterService.saveNewsletterRequest(newsletterUrl)
        newsletterService.saveNewsletterRequest(newsletterUrl)

        // THEN
        verify {
            monitoringService.notifyRequest(newsletterUrl)
            monitoringService.notifyRequest(newsletterUrl)
        }
    }
}
