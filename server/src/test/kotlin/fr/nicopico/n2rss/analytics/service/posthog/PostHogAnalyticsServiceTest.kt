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

package fr.nicopico.n2rss.analytics.service.posthog

import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.models.AnalyticsException
import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.beEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.beInstanceOf
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.HttpClientErrorException
import java.time.Instant

class PostHogAnalyticsServiceTest {
    private val server = MockWebServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun createAnalyticsService(
        enabled: Boolean = true,
        apiKey: String = "test-api-key",
        host: String = "http://localhost:" + server.port,
    ): PostHogAnalyticsService {
        return PostHogAnalyticsService(
            analyticsApiBaseUrl = host,
            analyticsProperties = N2RssProperties.AnalyticsProperties(
                enabled = enabled,
                postHog = N2RssProperties.PostHogProperties(
                    apiKey = apiKey,
                    host = host,
                ),
            ),
        )
    }

    @Test
    fun `request should follow PostHog API format`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("test-user-agent", "192.168.1.1"))

        // THEN
        val request = server.takeRequest()
        assertSoftly(request) {
            path shouldBe "/i/v0/e/"
            method shouldBe "POST"
            getHeader("Content-Type") shouldBe "application/json"
            val body = body.readUtf8()
            // Check required fields per PostHog API
            body shouldContain Regex("\"api_key\"\\s*:\\s*\"test-api-key\"")
            body shouldContain Regex("\"event\"\\s*:\\s*\"Home\"")
            // distinct_id should be outside properties
            body shouldContain Regex("\"distinct_id\"\\s*:\\s*\"[a-f0-9]{64}\"")
            body shouldContain Regex("\"properties\"\\s*:\\s*\\{")
            body shouldContain Regex("\"userAgent\"\\s*:\\s*\"test-user-agent\"")
            // timestamp should be in ISO 8601 format
            body shouldContain Regex("\"timestamp\"\\s*:\\s*\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
        }
    }

    @Test
    fun `GetFeed event should include feedCode and userAgent in properties`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.GetFeed("feed-code", "test-user-agent", "192.168.1.1"))

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertSoftly {
            body shouldContain Regex("\"event\"\\s*:\\s*\"GetFeed\"")
            body shouldContain Regex("\"feedCode\"\\s*:\\s*\"feed-code\"")
            body shouldContain Regex("\"userAgent\"\\s*:\\s*\"test-user-agent\"")
            body shouldContain Regex("\"distinct_id\"\\s*:\\s*\"[a-f0-9]{64}\"")
        }
    }

    @Test
    fun `RequestNewsletter event should include newsletterUrl and userAgent`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(
            AnalyticsEvent.RequestNewsletter(
                "https://example.com/newsletter",
                "test-user-agent",
                "192.168.1.1"
            )
        )

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertSoftly {
            body shouldContain Regex("\"event\"\\s*:\\s*\"RequestNewsletter\"")
            body shouldContain Regex("\"newsletterUrl\"\\s*:\\s*\"https://example.com/newsletter\"")
            body shouldContain Regex("\"userAgent\"\\s*:\\s*\"test-user-agent\"")
        }
    }

    @Test
    fun `Error events should have anonymous distinct_id`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.Error.GetFeedError("feed-code"))

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertSoftly {
            body shouldContain Regex("\"event\"\\s*:\\s*\"GetFeedError\"")
            body shouldContain Regex("\"distinct_id\"\\s*:\\s*\"anonymous\"")
            body shouldContain Regex("\"feedCode\"\\s*:\\s*\"feed-code\"")
        }
    }

    @Test
    fun `NewRelease event should have server distinct_id`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.NewRelease("1.0.0"))

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertSoftly {
            body shouldContain Regex("\"event\"\\s*:\\s*\"NewRelease\"")
            body shouldContain Regex("\"distinct_id\"\\s*:\\s*\"server\"")
            body shouldContain Regex("\"version\"\\s*:\\s*\"1.0.0\"")
        }
    }

    @Test
    fun `EmailParsingError event should include handler and email info`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(
            AnalyticsEvent.Error.EmailParsingError("handler-name", "email-title")
        )

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertSoftly {
            body shouldContain Regex("\"event\"\\s*:\\s*\"EmailParsingError\"")
            body shouldContain Regex("\"handlerName\"\\s*:\\s*\"handler-name\"")
            body shouldContain Regex("\"emailTitle\"\\s*:\\s*\"email-title\"")
            body shouldContain Regex("\"distinct_id\"\\s*:\\s*\"anonymous\"")
        }
    }

    @Test
    fun `timestamp should be in ISO 8601 format`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("ua", "192.168.1.1"))

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        // Extract timestamp value
        val timestampMatch = Regex("\"timestamp\"\\s*:\\s*\"([^\"]+)\"").find(body)
        timestampMatch shouldNotBe null
        val timestamp = timestampMatch?.groupValues?.get(1)
        timestamp shouldNotBe null
        // Verify it's a valid ISO 8601 instant
        Instant.parse(timestamp!!) // This will throw if not valid ISO 8601
    }

    @Test
    fun `API error should throw AnalyticsException`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(400))

        // WHEN - THEN
        val error = shouldThrow<AnalyticsException> {
            analyticsService.track(AnalyticsEvent.Home("ua", "192.168.1.1"))
        }
        error.message shouldNot beEmpty()
        error.cause should beInstanceOf<HttpClientErrorException>()
    }

    @Test
    fun `No events should be sent if analytics is disabled`() {
        // GIVEN
        val analyticsService = createAnalyticsService(enabled = false)

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("ua", "192.168.1.1"))
        analyticsService.track(AnalyticsEvent.GetFeed("code", "ua", "192.168.1.1"))

        // THEN
        server.requestCount shouldBe 0
    }

    @Test
    fun `No events should be sent if postHog properties are null`() {
        // GIVEN
        val analyticsService = PostHogAnalyticsService(
            analyticsApiBaseUrl = "http://localhost",
            analyticsProperties = N2RssProperties.AnalyticsProperties(
                enabled = true,
                postHog = null,
            ),
        )

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("ua", "192.168.1.1"))

        // THEN
        server.requestCount shouldBe 0
    }

    @Test
    fun `Same user agent and IP should produce same distinct_id`() {
        // GIVEN
        val clientIpAddress = "192.168.1.1"
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("same-user-agent", clientIpAddress))
        analyticsService.track(AnalyticsEvent.GetFeed("code", "same-user-agent", clientIpAddress))

        // THEN
        val request1 = server.takeRequest()
        val request2 = server.takeRequest()
        val body1 = request1.body.readUtf8()
        val body2 = request2.body.readUtf8()

        // Extract distinct_id values using regex
        val distinctId1 = Regex("\"distinct_id\"\\s*:\\s*\"([a-f0-9]{64})\"").find(body1)?.groupValues?.get(1)
        val distinctId2 = Regex("\"distinct_id\"\\s*:\\s*\"([a-f0-9]{64})\"").find(body2)?.groupValues?.get(1)

        distinctId1 shouldBe distinctId2
    }

    @Test
    fun `Different user agents should produce different distinct_id`() {
        // GIVEN
        val clientIpAddress = "192.168.1.1"
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("user-agent-1", clientIpAddress))
        analyticsService.track(AnalyticsEvent.Home("user-agent-2", clientIpAddress))

        // THEN
        val request1 = server.takeRequest()
        val request2 = server.takeRequest()
        val body1 = request1.body.readUtf8()
        val body2 = request2.body.readUtf8()

        val distinctId1 = Regex("\"distinct_id\"\\s*:\\s*\"([a-f0-9]{64})\"").find(body1)?.groupValues?.get(1)
        val distinctId2 = Regex("\"distinct_id\"\\s*:\\s*\"([a-f0-9]{64})\"").find(body2)?.groupValues?.get(1)

        distinctId1 shouldNotBe distinctId2
    }

    @Test
    fun `all required PostHog fields should be present`() {
        // GIVEN
        val analyticsService = createAnalyticsService()
        server.enqueue(MockResponse().setResponseCode(200))

        // WHEN
        analyticsService.track(AnalyticsEvent.Home("ua", "192.168.1.1"))

        // THEN
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        // Per PostHog API docs: api_key, event, and properties are required
        assertSoftly {
            body shouldContain "\"api_key\""
            body shouldContain "\"event\""
            body shouldContain "\"properties\""
        }
    }
}
