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

package fr.nicopico.n2rss.analytics.simpleanalytics

import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.models.AnalyticsException
import fr.nicopico.n2rss.analytics.service.simpleanalytics.SimpleAnalyticsService
import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.beEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.beInstanceOf
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.HttpClientErrorException

class SimpleAnalyticsServiceTest {
    private val server = MockWebServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun createAnalyticService(
        enabled: Boolean = true,
        userAgent: String = "some-user-agent",
        hostname: String = "some-hostname",
    ): SimpleAnalyticsService {
        return SimpleAnalyticsService(
            analyticsApiBaseUrl = server.url("/").toString(),
            analyticsProperties = N2RssProperties.AnalyticsProperties(
                enabled = enabled,
                simpleAnalytics = N2RssProperties.SimpleAnalyticsProperties(
                    userAgent = userAgent,
                    hostname = hostname,
                ),
            ),
        )
    }

    @Test
    fun `GetFeed analytic events should be sent to the API`() {
        // GIVEN
        val analyticService = createAnalyticService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticService.track(AnalyticsEvent.GetFeed("rss-code", "ua"))

        // THEN
        val request = server.takeRequest()
        assertSoftly(request) {
            path shouldBe "/events"
            method shouldBe "POST"
            body.readUtf8() should {
                it shouldContain Regex("\"hostname\"\\s*:\"some-hostname\"")
                it shouldContain Regex("\"ua\"\\s*:\"some-user-agent\"")
                it shouldContain Regex("\"event\"\\s*:\"get-feed\"")
                it shouldContain Regex("\"feedCode\"\\s*:\"rss-code\"")
            }
        }
    }

    @Test
    fun `RequestNewsletter analytic events should be sent to the API`() {
        // GIVEN
        val analyticService = createAnalyticService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticService.track(AnalyticsEvent.RequestNewsletter("some-newsletter-url", "ua"))

        // THEN
        val request = server.takeRequest()
        assertSoftly(request) {
            path shouldBe "/events"
            method shouldBe "POST"
            body.readUtf8() should {
                it shouldContain Regex("\"hostname\"\\s*:\"some-hostname\"")
                it shouldContain Regex("\"event\"\\s*:\"request-newsletter\"")
                it shouldContain Regex("\"newsletterUrl\"\\s*:\"some-newsletter-url\"")
            }
        }
    }

    @Test
    fun `Analytics API error should throw a specific exception`() {
        // GIVEN
        val analyticService = createAnalyticService()
        server.enqueue(MockResponse().setResponseCode(400))

        // WHEN - THEN
        val error = shouldThrow<AnalyticsException> {
            analyticService.track(AnalyticsEvent.GetFeed("code", "ua"))
        }
        error.message shouldNot beEmpty()
        error.cause should beInstanceOf<HttpClientErrorException>()
    }

    @Test
    fun `No events should be sent if analytics is disabled`() {
        // GIVEN
        val analyticService = createAnalyticService(enabled = false)

        // WHEN
        analyticService.track(AnalyticsEvent.GetFeed("code", "ua"))
        analyticService.track(AnalyticsEvent.RequestNewsletter("url", "ua"))

        // THEN
        server.requestCount shouldBe 0
    }
}
