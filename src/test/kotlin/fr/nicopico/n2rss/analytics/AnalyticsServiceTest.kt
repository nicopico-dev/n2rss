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
import fr.nicopico.n2rss.analytics.data.AnalyticsRepository
import fr.nicopico.n2rss.analytics.data.toAnalyticsData
import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.kotest.matchers.string.beEmpty as beAnEmptyString

class AnalyticsServiceTest {

    @MockK
    private lateinit var analyticsRepository: AnalyticsRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { analyticsRepository.save(any()) } answers { firstArg() }
    }

    private fun createAnalyticService(enabled: Boolean = true): AnalyticsService {
        return AnalyticsService(
            analyticsRepository = analyticsRepository,
            analyticsProperties = N2RssProperties.AnalyticsProperties(
                enabled = enabled,
            ),
        )
    }

    @Test
    fun `Analytic events should be converted before being saved`() {
        // GIVEN
        val analyticService = createAnalyticService()
        val data = mockk<AnalyticsData>()
        val event = mockk<AnalyticsEvent>()

        // SETUP
        mockkStatic(AnalyticsEvent::toAnalyticsData) {
            every { any<AnalyticsEvent>().toAnalyticsData(any()) } returns data

            // WHEN
            analyticService.track(event)

            // THEN
            verifyOrder {
                event.toAnalyticsData(any())
                analyticsRepository.save(data)
            }
        }
    }

    @Test
    fun `Analytics API error should throw a specific exception`() {
        // GIVEN
        val analyticService = createAnalyticService()
        val internalError = RuntimeException("TEST")
        every { analyticsRepository.save(any()) } throws internalError

        // WHEN - THEN
        val error = shouldThrow<AnalyticsException> {
            analyticService.track(AnalyticsEvent.GetFeed("code", "userAgent"))
        }
        error.message shouldNot beAnEmptyString()
        error.cause shouldBe internalError
    }

    @Test
    fun `No events should be sent if analytics is disabled`() {
        // GIVEN
        val analyticService = createAnalyticService(enabled = false)
        val event = mockk<AnalyticsEvent>()

        // WHEN
        analyticService.track(event)

        // THEN
        confirmVerified(analyticsRepository, event)
    }
}
