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
package fr.nicopico.n2rss.analytics

import fr.nicopico.n2rss.analytics.data.AnalyticsData
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode
import fr.nicopico.n2rss.analytics.data.AnalyticsRepository
import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.beEmpty
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val s = "userAgent"

class AnalyticServiceTest {

    @MockK
    private lateinit var analyticsRepository: AnalyticsRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { analyticsRepository.save(any()) } answers { firstArg() }
    }

    private fun createAnalyticService(enabled: Boolean = true): AnalyticService {
        return AnalyticService(
            analyticsRepository = analyticsRepository,
            analyticsProperties = N2RssProperties.AnalyticsProperties(
                enabled = enabled,
            ),
        )
    }

    @Test
    fun `Home analytic events should be stored`() {
        // GIVEN
        val analyticService = createAnalyticService()

        // WHEN
        analyticService.track(AnalyticEvent.Home)

        // THEN
        val dataSlot = slot<AnalyticsData>()
        verify { analyticsRepository.save(capture(dataSlot)) }
        assertSoftly(dataSlot.captured) {
            it.code shouldBe AnalyticsDataCode.HOME
            it.data shouldBe null
        }
    }

    @Test
    fun `GetFeed analytic events should be stored`() {
        // GIVEN
        val rssCode = "rss-code"
        val userAgent = "userAgent"
        val analyticService = createAnalyticService()

        // WHEN
        analyticService.track(AnalyticEvent.GetFeed(rssCode, userAgent))

        // THEN
        val dataSlot = slot<AnalyticsData>()
        verify { analyticsRepository.save(capture(dataSlot)) }
        assertSoftly(dataSlot.captured) {
            it.code shouldBe AnalyticsDataCode.GET_FEED
            it.data shouldBe rssCode
        }
    }

    @Test
    fun `RequestNewsletter analytic events should be stored`() {
        // GIVEN
        val newsletterUrl = "some-newsletter-url"
        val analyticService = createAnalyticService()

        // WHEN
        analyticService.track(AnalyticEvent.RequestNewsletter(newsletterUrl))

        // THEN
        val dataSlot = slot<AnalyticsData>()
        verify { analyticsRepository.save(capture(dataSlot)) }
        assertSoftly(dataSlot.captured) {
            it.code shouldBe AnalyticsDataCode.REQUEST_NEWSLETTER
            it.data shouldBe newsletterUrl
        }
    }

    @Test
    fun `Analytics API error should throw a specific exception`() {
        // GIVEN
        val analyticService = createAnalyticService()
        val internalError = RuntimeException("TEST")
        every { analyticsRepository.save(any()) } throws internalError

        // WHEN - THEN
        val error = shouldThrow<AnalyticException> {
            analyticService.track(AnalyticEvent.GetFeed("code", "userAgent"))
        }
        error.message shouldNot beEmpty()
        error.cause shouldBe internalError
    }

    @Test
    fun `No events should be sent if analytics is disabled`() {
        // GIVEN
        val analyticService = createAnalyticService(enabled = false)

        // WHEN
        analyticService.track(AnalyticEvent.Home)
        analyticService.track(AnalyticEvent.GetFeed("code", "userAgent"))
        analyticService.track(AnalyticEvent.RequestNewsletter("url"))

        // THEN
        confirmVerified(analyticsRepository)
    }
}
