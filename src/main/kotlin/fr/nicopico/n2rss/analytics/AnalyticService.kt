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

import com.fasterxml.jackson.annotation.JsonProperty
import fr.nicopico.n2rss.config.N2RssProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

private val LOG = LoggerFactory.getLogger(AnalyticService::class.java)

@Service
class AnalyticService(
    restClientBuilder: RestClient.Builder = RestClient.builder(),
    private val analyticsApiBaseUrl: String,
    private val analyticsProperties: N2RssProperties.AnalyticsProperties,
) {
    @Autowired
    constructor(
        restClientBuilder: RestClient.Builder,
        analyticsProperties: N2RssProperties.AnalyticsProperties,
    ) : this(
        restClientBuilder = restClientBuilder,
        analyticsApiBaseUrl = "https://queue.simpleanalyticscdn.com",
        analyticsProperties = analyticsProperties,
    )

    private val restClient by lazy {
        restClientBuilder
            .baseUrl(analyticsApiBaseUrl)
            .build()
    }

    @Throws(AnalyticException::class)
    fun track(event: AnalyticEvent) {
        if (analyticsProperties.enabled) {
            LOG.info("TRACK: $event")
            try {
                restClient
                    .post()
                    .uri("/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event.toSimpleAnalyticsEvent())
                    .retrieve()
                    .toBodilessEntity()
            } catch (e: HttpClientErrorException) {
                throw AnalyticException("Unable to send analytics event $event", e)
            }
        }
    }

    private fun AnalyticEvent.toSimpleAnalyticsEvent(): SimpleAnalyticsEvent {
        return SimpleAnalyticsEvent(
            type = "event",
            hostname = analyticsProperties.hostname,
            event = when (this) {
                is AnalyticEvent.GetFeed -> EVENT_GET_FEED
                is AnalyticEvent.RequestNewsletter -> EVENT_REQUEST_NEWSLETTER
            },
            ua = analyticsProperties.userAgent,
            metadata = when (this) {
                is AnalyticEvent.GetFeed -> mapOf(
                    METADATA_GET_FEED_CODE to code
                )

                is AnalyticEvent.RequestNewsletter -> mapOf(
                    METADATA_REQUEST_NEWSLETTER_URL to newsletterUrl
                )
            }
        )
    }

    companion object {
        private const val EVENT_GET_FEED = "get-feed"
        private const val EVENT_REQUEST_NEWSLETTER = "request-newsletter"

        private const val METADATA_GET_FEED_CODE = "get-feed-code"
        private const val METADATA_REQUEST_NEWSLETTER_URL = "request-newsletter-url"
    }
}

private data class SimpleAnalyticsEvent(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("hostname")
    val hostname: String,
    @JsonProperty("event")
    val event: String,
    @JsonProperty("ua")
    val ua: String,
    @JsonProperty("metadata")
    val metadata: Map<String, String>
)
