/*
 * Copyright (c) 2025 Nicolas PICON
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
package fr.nicopico.n2rss.monitoring

import fr.nicopico.n2rss.fakes.FixedClock
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandlerMultipleFeeds
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandlerSingleFeed
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.NewsletterStats
import fr.nicopico.n2rss.newsletter.service.PublicationService
import fr.nicopico.n2rss.utils.now
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Clock

@ExtendWith(MockKExtension::class)
class MissingPublicationDetectionServiceTest {

    @MockK
    private lateinit var newsletterRepository: NewsletterRepository
    @MockK
    private lateinit var publicationService: PublicationService
    @MockK(relaxUnitFun = true)
    private lateinit var monitoringService: MonitoringService

    private lateinit var clock: Clock

    private lateinit var service: MissingPublicationDetectionService

    @BeforeEach
    fun setUp() {
        clock = FixedClock(Clock.System.now())

        service = MissingPublicationDetectionService(
            newsletterRepository = newsletterRepository,
            publicationService = publicationService,
            monitoringService = monitoringService,
            clock = clock,
            tolerance = DatePeriod(days = 2)
        )
    }

    //region helpers
    private fun newsletter(code: String) = Newsletter(
        code = code,
        name = code,
        websiteUrl = "https://example.com/$code",
    )

    private fun singleHandler(nl: Newsletter): NewsletterHandler = object : NewsletterHandlerSingleFeed {
        override val newsletter: Newsletter = nl
        override fun canHandle(email: fr.nicopico.n2rss.mail.models.Email) = false
        override fun extractArticles(email: fr.nicopico.n2rss.mail.models.Email) =
            emptyList<fr.nicopico.n2rss.newsletter.models.Article>()
    }

    private fun multiHandler(vararg nls: Newsletter): NewsletterHandler = object : NewsletterHandlerMultipleFeeds {
        override val newsletters: List<Newsletter> = nls.toList()
        override fun canHandle(email: fr.nicopico.n2rss.mail.models.Email) = false
        override fun extractArticles(email: fr.nicopico.n2rss.mail.models.Email) =
            emptyMap<Newsletter, List<fr.nicopico.n2rss.newsletter.models.Article>>()
    }
    //endregion

    @Test
    fun `detectMissingPublications should notify when at least one newsletter is late`() {
        // GIVEN
        val today = LocalDate.now(clock)

        val nlA = newsletter("A")
        val nlB = newsletter("B")
        val nlC = newsletter("C")

        val handlerA = singleHandler(nlA)
        val handlerB = singleHandler(nlB)
        val handlerC = singleHandler(nlC)

        every { newsletterRepository.getEnabledNewsletterHandlers() } returns
            listOf(handlerA, handlerB, handlerC)

        // A is late: next expected publication is 3 days late
        every { publicationService.getNewsletterStats(nlA) } returns NewsletterStats.MultiplePublications(
            firstPublicationDate = today - DatePeriod(days = 100),
            lastPublicationDate = today - DatePeriod(days = 10),
            publicationCount = 10,
            publicationPeriodicity = DatePeriod(days = 7),
            articlesPerPublication = 5,
        )
        // B is on time: next publication is expected in 2 days
        every { publicationService.getNewsletterStats(nlB) } returns NewsletterStats.MultiplePublications(
            firstPublicationDate = today - DatePeriod(days = 50),
            lastPublicationDate = today - DatePeriod(days = 5),
            publicationCount = 8,
            publicationPeriodicity = DatePeriod(days = 7),
            articlesPerPublication = 6,
        )
        // C is late but still within tolerance (2 days late)
        every { publicationService.getNewsletterStats(nlC) } returns NewsletterStats.MultiplePublications(
            firstPublicationDate = today - DatePeriod(days = 100),
            lastPublicationDate = today - DatePeriod(days = 9),
            publicationCount = 10,
            publicationPeriodicity = DatePeriod(days = 7),
            articlesPerPublication = 5,
        )

        // WHEN
        service.detectMissingPublications()

        // THEN
        val codesSlot = slot<List<String>>()
        verify { monitoringService.notifyMissingPublications(capture(codesSlot)) }
        codesSlot.captured.shouldContainExactly(listOf("A"))
        confirmVerified(monitoringService)
    }

    @Test
    fun `detectMissingPublications should not notify when no newsletter is late`() {
        // GIVEN
        val today = LocalDate.now(clock)

        val nlA = newsletter("A")
        val nlB = newsletter("B")

        val handlerA = singleHandler(nlA)
        val handlerB = singleHandler(nlB)

        every { newsletterRepository.getEnabledNewsletterHandlers() } returns listOf(handlerA, handlerB)

        // A has only one publication -> ignored
        every { publicationService.getNewsletterStats(nlA) } returns NewsletterStats.SinglePublication(
            publicationDate = today - DatePeriod(days = 1),
        )
        // B has multiple publications but on time
        every { publicationService.getNewsletterStats(nlB) } returns NewsletterStats.MultiplePublications(
            firstPublicationDate = today - DatePeriod(days = 20),
            lastPublicationDate = today,
            publicationCount = 3,
            publicationPeriodicity = DatePeriod(days = 7),
            articlesPerPublication = 3,
        )

        // WHEN
        service.detectMissingPublications()

        // THEN
        verify(exactly = 0) { monitoringService.notifyMissingPublications(any()) }
        confirmVerified(monitoringService)
    }

    @Test
    fun `detectMissingPublications should consider only the first newsletter of each handler`() {
        // GIVEN
        val today = LocalDate.now(clock)

        val nl1 = newsletter("FIRST")
        val nl2 = newsletter("SECOND")
        val nl3 = newsletter("THIRD")

        val handler1 = multiHandler(nl1, nl2) // only nl1 should be considered
        val handler2 = singleHandler(nl3)

        every { newsletterRepository.getEnabledNewsletterHandlers() } returns listOf(handler1, handler2)

        // FIRST is late -> should trigger
        every { publicationService.getNewsletterStats(nl1) } returns NewsletterStats.MultiplePublications(
            firstPublicationDate = today - DatePeriod(days = 90),
            lastPublicationDate = today - DatePeriod(days = 15),
            publicationCount = 12,
            publicationPeriodicity = DatePeriod(days = 7),
            articlesPerPublication = 7,
        )

        // SECOND should never be queried if only first is considered; but for safety we can set it to throw if called
        every { publicationService.getNewsletterStats(nl2) } throws AssertionError("Should not be called for SECOND newsletter")

        // THIRD is on time -> ignored
        every { publicationService.getNewsletterStats(nl3) } returns NewsletterStats.MultiplePublications(
            firstPublicationDate = today - DatePeriod(days = 30),
            lastPublicationDate = today - DatePeriod(days = 5),
            publicationCount = 5,
            publicationPeriodicity = DatePeriod(days = 7),
            articlesPerPublication = 4,
        )

        // WHEN
        service.detectMissingPublications()

        // THEN
        val codesSlot = slot<List<String>>()
        verify { monitoringService.notifyMissingPublications(capture(codesSlot)) }
        codesSlot.captured.shouldContainExactly(listOf("FIRST"))
        confirmVerified(monitoringService)
    }
}
