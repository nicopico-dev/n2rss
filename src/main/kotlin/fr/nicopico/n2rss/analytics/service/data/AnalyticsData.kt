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
package fr.nicopico.n2rss.analytics.service.data

import fr.nicopico.n2rss.analytics.models.AnalyticsCode
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.DATA_EMAIL_TITLE
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.DATA_FEED_CODE
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.DATA_HANDLER_NAME
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.DATA_NEWSLETTER_URL
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.DATA_USER_AGENT
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.DATA_VERSION
import fr.nicopico.n2rss.analytics.models.AnalyticsCode.EVENT_GET_FEED
import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.utils.getFingerprint
import kotlinx.datetime.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Suppress("unused")
@Document(collection = "analytics")
data class AnalyticsData(
    val code: String,
    val timestamp: Instant,
    val data: Map<String, String?> = emptyMap(),
    @Id val id: String? = null,
)

@Suppress("LongMethod")
fun AnalyticsEvent.toAnalyticsData(timestamp: Instant): AnalyticsData {
    fun String.fingerprint() = getFingerprint(this)

    return when (this) {
        is AnalyticsEvent.GetFeed -> AnalyticsData(
            code = EVENT_GET_FEED,
            data = mapOf(
                DATA_FEED_CODE to feedCode,
                DATA_USER_AGENT to userAgent.fingerprint(),
            ),
            timestamp = timestamp,
        )

        is AnalyticsEvent.GetRssFeeds -> AnalyticsData(
            code = AnalyticsCode.EVENT_GET_RSS_FEEDS,
            data = mapOf(
                DATA_USER_AGENT to userAgent.fingerprint(),
            ),
            timestamp = timestamp,
        )

        is AnalyticsEvent.Home -> AnalyticsData(
            code = AnalyticsCode.EVENT_HOME,
            data = mapOf(
                DATA_USER_AGENT to userAgent.fingerprint(),
            ),
            timestamp = timestamp,
        )

        is AnalyticsEvent.RequestNewsletter -> AnalyticsData(
            code = AnalyticsCode.EVENT_REQUEST_NEWSLETTER,
            data = mapOf(
                DATA_NEWSLETTER_URL to newsletterUrl,
                DATA_USER_AGENT to userAgent.fingerprint(),
            ),
            timestamp = timestamp,
        )

        is AnalyticsEvent.NewRelease -> AnalyticsData(
            code = AnalyticsCode.EVENT_RELEASE,
            data = mapOf(DATA_VERSION to version),
            timestamp = timestamp,
        )

        is AnalyticsEvent.Error.GetFeedError -> AnalyticsData(
            code = AnalyticsCode.EVENT_ERROR_GET_FEED,
            data = mapOf(DATA_FEED_CODE to feedCode),
            timestamp = timestamp,
        )

        AnalyticsEvent.Error.GetRssFeedsError -> AnalyticsData(
            code = AnalyticsCode.EVENT_ERROR_GET_RSS_FEEDS,
            timestamp = timestamp,
        )

        is AnalyticsEvent.Error.EmailParsingError -> AnalyticsData(
            code = AnalyticsCode.EVENT_ERROR_PARSING,
            data = mapOf(
                DATA_HANDLER_NAME to handlerName,
                DATA_EMAIL_TITLE to emailTitle,
            ),
            timestamp = timestamp,
        )

        is AnalyticsEvent.Error.HomeError -> AnalyticsData(
            code = AnalyticsCode.EVENT_ERROR_HOME,
            timestamp = timestamp,
        )

        is AnalyticsEvent.Error.RequestNewsletterError -> AnalyticsData(
            code = AnalyticsCode.EVENT_ERROR_REQUEST_NEWSLETTER,
            timestamp = timestamp,
        )
    }
}
