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
package fr.nicopico.n2rss.analytics.service.simpleanalytics

import com.fasterxml.jackson.annotation.JsonProperty
import fr.nicopico.n2rss.analytics.models.AnalyticsCode
import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.utils.getFingerprint

data class SimpleAnalyticsEvent(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("hostname")
    val hostname: String,
    @JsonProperty("event")
    val event: String,
    @JsonProperty("ua")
    val ua: String,
    @JsonProperty("metadata")
    val metadata: Map<String, String>? = null,
    @JsonProperty("path")
    val path: String? = null,
)

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun AnalyticsEvent.toSimpleAnalyticsEvent(
    simpleAnalyticsProperties: N2RssProperties.SimpleAnalyticsProperties,
): SimpleAnalyticsEvent? {
    fun String.fingerprint() = getFingerprint(this)

    return when (this) {
        is AnalyticsEvent.Error.EmailParsingError -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_ERROR_PARSING,
            metadata = mapOf(
                AnalyticsCode.DATA_HANDLER_NAME to handlerName,
                AnalyticsCode.DATA_EMAIL_TITLE to emailTitle
            )
        )

        is AnalyticsEvent.Error.GetFeedError -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_ERROR_GET_FEED,
            metadata = mapOf(
                AnalyticsCode.DATA_FEED_CODE to feedCode
            )
        )

        is AnalyticsEvent.Error.GetRssFeedsError -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_ERROR_GET_RSS_FEEDS
        )

        is AnalyticsEvent.Error.HomeError -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_ERROR_HOME
        )

        is AnalyticsEvent.Error.RequestNewsletterError -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_ERROR_REQUEST_NEWSLETTER
        )

        is AnalyticsEvent.GetFeed -> createSimpleAnalyticsPageView(
            simpleAnalyticsProperties,
            path = "/rss/$feedCode",
            userAgent = userAgent,
        )

        is AnalyticsEvent.GetRssFeeds -> createSimpleAnalyticsPageView(
            simpleAnalyticsProperties,
            path = "/rss",
            userAgent = userAgent,
        )

        // Home page view is caught by SimpleAnalytics script
        is AnalyticsEvent.Home -> null

        is AnalyticsEvent.NewRelease -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_RELEASE,
            metadata = mapOf(
                AnalyticsCode.DATA_VERSION to version
            )
        )

        is AnalyticsEvent.RequestNewsletter -> createSimpleAnalyticsEvent(
            simpleAnalyticsProperties,
            event = AnalyticsCode.EVENT_REQUEST_NEWSLETTER,
            metadata = buildMap {
                put(AnalyticsCode.DATA_NEWSLETTER_URL, newsletterUrl)
                userAgent.fingerprint()?.let {
                    put(AnalyticsCode.DATA_USER_AGENT, it)
                }
            }
        )
    }
}

private fun createSimpleAnalyticsEvent(
    simpleAnalyticsProperties: N2RssProperties.SimpleAnalyticsProperties,
    event: String,
    metadata: Map<String, String> = emptyMap(),
) = SimpleAnalyticsEvent(
    type = "event",
    event = event,
    metadata = metadata,
    hostname = simpleAnalyticsProperties.hostname,
    ua = simpleAnalyticsProperties.userAgent,
)

private fun createSimpleAnalyticsPageView(
    simpleAnalyticsProperties: N2RssProperties.SimpleAnalyticsProperties,
    path: String,
    userAgent: String,
) = SimpleAnalyticsEvent(
    type = "pageview",
    event = "pageview",
    path = path,
    hostname = simpleAnalyticsProperties.hostname,
    ua = userAgent,
)
