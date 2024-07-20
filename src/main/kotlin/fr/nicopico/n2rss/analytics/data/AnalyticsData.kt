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
package fr.nicopico.n2rss.analytics.data

import fr.nicopico.n2rss.analytics.AnalyticEvent
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.DATA_EMAIL_TITLE
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.DATA_FEED_CODE
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.DATA_HANDLER_NAME
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.DATA_NEWSLETTER_URL
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.DATA_USER_AGENT
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.DATA_VERSION
import fr.nicopico.n2rss.analytics.data.AnalyticsDataCode.GET_FEED
import kotlinx.datetime.Instant
import org.jetbrains.annotations.VisibleForTesting
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Suppress("unused")
@Document(collection = "analytics")
class AnalyticsData(
    val code: String,
    val timestamp: Instant,
    val data: Map<String, String?> = emptyMap(),
    @Id val id: String? = null,
)

@VisibleForTesting
object AnalyticsDataCode {
    const val HOME = "home"
    const val GET_FEED = "get-feed"
    const val REQUEST_NEWSLETTER = "request-newsletter"
    const val RELEASE = "release"
    const val ERROR_GET_FEED = "error-get-feed"
    const val ERROR_PARSING = "error-parsing"
    const val ERROR_HOME = "error-home"
    const val ERROR_REQUEST_NEWSLETTER = "error-request-newsletter"

    const val DATA_FEED_CODE = "data-feed-code"
    const val DATA_USER_AGENT = "data-user-agent"
    const val DATA_NEWSLETTER_URL = "data-newsletter-url"
    const val DATA_VERSION = "data-version"
    const val DATA_HANDLER_NAME = "data-handler-name"
    const val DATA_EMAIL_TITLE = "data-email-title"
}

fun AnalyticEvent.toAnalyticsData(timestamp: Instant): AnalyticsData {
    return when (this) {
        is AnalyticEvent.GetFeed -> AnalyticsData(
            code = GET_FEED,
            data = mapOf(
                DATA_FEED_CODE to feedCode,
                DATA_USER_AGENT to userAgent,
            ),
            timestamp = timestamp,
        )

        is AnalyticEvent.Home -> AnalyticsData(
            code = AnalyticsDataCode.HOME,
            timestamp = timestamp,
        )

        is AnalyticEvent.RequestNewsletter -> AnalyticsData(
            code = AnalyticsDataCode.REQUEST_NEWSLETTER,
            data = mapOf(DATA_NEWSLETTER_URL to newsletterUrl),
            timestamp = timestamp,
        )

        is AnalyticEvent.NewRelease -> AnalyticsData(
            code = AnalyticsDataCode.RELEASE,
            data = mapOf(DATA_VERSION to version),
            timestamp = timestamp,
        )

        is AnalyticEvent.Error.GetFeedError -> AnalyticsData(
            code = AnalyticsDataCode.ERROR_GET_FEED,
            data = mapOf(DATA_FEED_CODE to feedCode),
            timestamp = timestamp,
        )

        is AnalyticEvent.Error.EmailParsingError -> AnalyticsData(
            code = AnalyticsDataCode.ERROR_PARSING,
            data = mapOf(
                DATA_HANDLER_NAME to handlerName,
                DATA_EMAIL_TITLE to emailTitle,
            ),
            timestamp = timestamp,
        )

        is AnalyticEvent.Error.HomeError -> AnalyticsData(
            code = AnalyticsDataCode.ERROR_HOME,
            timestamp = timestamp,
        )

        is AnalyticEvent.Error.RequestNewsletterError -> AnalyticsData(
            code = AnalyticsDataCode.ERROR_REQUEST_NEWSLETTER,
            timestamp = timestamp,
        )
    }
}
