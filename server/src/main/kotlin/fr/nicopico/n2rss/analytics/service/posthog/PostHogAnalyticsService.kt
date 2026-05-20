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
import fr.nicopico.n2rss.analytics.service.AnalyticsService
import fr.nicopico.n2rss.config.N2RssProperties
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
class PostHogAnalyticsService(
    restClientBuilder: RestClient.Builder = RestClient.builder(),
    private val analyticsProperties: N2RssProperties.AnalyticsProperties,
) : AnalyticsService {

    private val postHogProperties = analyticsProperties.postHog

    private val restClient by lazy {
        requireNotNull(postHogProperties) {
            "n2rss.analytics.posthog.host must be set"
        }
        restClientBuilder
            .baseUrl(postHogProperties.host)
            .build()
    }

    @Throws(AnalyticsException::class)
    override fun track(event: AnalyticsEvent) {
        if (analyticsProperties.enabled && postHogProperties != null) {
            try {
                val postHogEvent = event.toPostHogEvent()
                LOG.info("TRACK: $event")
                restClient
                    .post()
                    .uri("/capture/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(postHogEvent)
                    .retrieve()
                    .toBodilessEntity()
            } catch (e: RestClientException) {
                throw AnalyticsException("Unable to send analytics event $event", e)
            }
        }
    }

    private fun AnalyticsEvent.toPostHogEvent(): PostHogEvent {
        val distinctId = getDistinctId()
        val (eventName, properties) = getEventNameAndProperties()

        return PostHogEvent(
            apiKey = postHogProperties?.apiKey ?: "",
            event = eventName,
            distinctId = distinctId,
            properties = properties
        )
    }

    private fun AnalyticsEvent.getDistinctId(): String = when (this) {
        is AnalyticsEvent.Home -> hashUserAgent(userAgent)
        is AnalyticsEvent.GetRssFeeds -> hashUserAgent(userAgent)
        is AnalyticsEvent.GetFeed -> hashUserAgent(userAgent)
        is AnalyticsEvent.RequestNewsletter -> hashUserAgent(userAgent)
        is AnalyticsEvent.Error -> "anonymous"
        is AnalyticsEvent.NewRelease -> "server"
    }

    private fun AnalyticsEvent.getEventNameAndProperties(): Pair<String, Map<String, Any>> =
        when (this) {
            is AnalyticsEvent.Home -> "Home" to mapOf("userAgent" to userAgent)
            is AnalyticsEvent.GetRssFeeds -> "GetRssFeeds" to mapOf("userAgent" to userAgent)
            is AnalyticsEvent.GetFeed -> "GetFeed" to
                mapOf("feedCode" to feedCode, "userAgent" to userAgent)

            is AnalyticsEvent.RequestNewsletter ->
                "RequestNewsletter" to mapOf(
                    "newsletterUrl" to newsletterUrl,
                    "userAgent" to userAgent
                )

            is AnalyticsEvent.Error.HomeError -> "HomeError" to emptyMap()
            is AnalyticsEvent.Error.GetRssFeedsError -> "GetRssFeedsError" to emptyMap()
            is AnalyticsEvent.Error.GetFeedError -> "GetFeedError" to mapOf("feedCode" to feedCode)
            is AnalyticsEvent.Error.RequestNewsletterError ->
                "RequestNewsletterError" to emptyMap()

            is AnalyticsEvent.Error.EmailParsingError ->
                "EmailParsingError" to mapOf(
                    "handlerName" to handlerName,
                    "emailTitle" to emailTitle
                )

            is AnalyticsEvent.NewRelease -> "NewRelease" to mapOf("version" to version)
        }

    private fun hashUserAgent(userAgent: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(userAgent.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PostHogAnalyticsService::class.java)
    }
}

data class PostHogEvent(
    val apiKey: String,
    val event: String,
    val distinctId: String,
    val properties: Map<String, Any>,
)
