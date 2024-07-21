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

import fr.nicopico.n2rss.analytics.data.DataAnalyticsService
import fr.nicopico.n2rss.analytics.simpleanalytics.SimpleAnalyticsService
import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AnalyticsConfigTest {

    @MockK
    private lateinit var analyticsProperties: N2RssProperties.AnalyticsProperties
    @MockK
    private lateinit var dataAnalyticsService: DataAnalyticsService
    @MockK
    private lateinit var simpleAnalyticsService: SimpleAnalyticsService

    private lateinit var config: AnalyticsConfig

    @BeforeEach
    fun setUp() {
        config = AnalyticsConfig()
    }

    @Test
    fun `AnalyticsService should be Data implementation if data-analytics profile is enabled`() {
        // WHEN
        every { analyticsProperties.analyticsProfiles } returns listOf("data-analytics")

        // WHEN
        val actualService = config.analyticsService(analyticsProperties, dataAnalyticsService, simpleAnalyticsService)

        // THEN
        actualService shouldBe dataAnalyticsService
    }

    @Test
    fun `AnalyticsService should be SimpleAnalytics implementation if simple-analytics profile is enabled`() {
        // WHEN
        every { analyticsProperties.analyticsProfiles } returns listOf("simple-analytics")

        // WHEN
        val actualService = config.analyticsService(analyticsProperties, dataAnalyticsService, simpleAnalyticsService)

        // THEN
        actualService shouldBe simpleAnalyticsService
    }

    @Test
    fun `AnalyticsService should be Delegate implementation if multiple profiles are enabled`() {
        // WHEN
        every { analyticsProperties.analyticsProfiles } returns listOf("data-analytics", "simple-analytics")

        // WHEN
        val actualService = config.analyticsService(analyticsProperties, dataAnalyticsService, simpleAnalyticsService)

        // THEN
        actualService should beOfType<AnalyticsServiceDelegate>()
        (actualService as AnalyticsServiceDelegate).analyticsServices shouldContainOnly
            listOf(dataAnalyticsService, simpleAnalyticsService)
    }

    @Test
    fun `Repeated profiles should not lead to duplicate delegated services`() {
        // WHEN
        every { analyticsProperties.analyticsProfiles } returns
            listOf("data-analytics", "simple-analytics", "data-analytics", "simple-analytics")

        // WHEN
        val actualService = config.analyticsService(analyticsProperties, dataAnalyticsService, simpleAnalyticsService)

        // THEN
        actualService should beOfType<AnalyticsServiceDelegate>()
        (actualService as AnalyticsServiceDelegate).analyticsServices shouldContainExactlyInAnyOrder
            listOf(dataAnalyticsService, simpleAnalyticsService)
    }

    @Test
    fun `AnalyticsService should be NoOp implementation if no profiles are enabled`() {
        // WHEN
        every { analyticsProperties.analyticsProfiles } returns emptyList()

        // WHEN
        val actualService = config.analyticsService(analyticsProperties, dataAnalyticsService, simpleAnalyticsService)

        // THEN
        actualService should beOfType<NoOpAnalyticsService>()
    }

    @Test
    fun `AnalyticsService should be NoOp implementation if no valid profiles are enabled`() {
        // WHEN
        every { analyticsProperties.analyticsProfiles } returns listOf("Google", "Facebook")

        // WHEN
        val actualService = config.analyticsService(analyticsProperties, dataAnalyticsService, simpleAnalyticsService)

        // THEN
        actualService should beOfType<NoOpAnalyticsService>()
    }
}
