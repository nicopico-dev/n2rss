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

import com.fasterxml.jackson.annotation.JsonProperty
import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.models.UserRequestAnalyticEvent
import fr.nicopico.n2rss.config.N2RssProperties
import java.time.Instant
import java.time.format.DateTimeFormatter

data class PostHogEvent(
    @param:JsonProperty("api_key")
    val apiKey: String,
    @param:JsonProperty("event")
    val event: String,
    @param:JsonProperty("properties")
    val properties: Map<String, Any>,
    @param:JsonProperty("timestamp")
    val timestamp: String,
)

fun AnalyticsEvent.toPostHogEvent(
    postHogProperties: N2RssProperties.PostHogProperties
): PostHogEvent {
    val distinctId = getDistinctId()
    val (eventName, eventProperties) = getEventNameAndProperties()
    val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

    return PostHogEvent(
        apiKey = postHogProperties.apiKey,
        event = eventName,
        properties = eventProperties + ("distinct_id" to distinctId),
        timestamp = timestamp,
    )
}

private fun AnalyticsEvent.getDistinctId(): String = when (this) {
    is UserRequestAnalyticEvent -> getUniqueUserId() ?: "anonymous"
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
