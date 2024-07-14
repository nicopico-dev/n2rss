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
import kotlinx.datetime.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Suppress("unused")
@Document(collection = "analytics")
class AnalyticsData(
    val code: String,
    val timestamp: Instant,
    val data: String? = null,
    @Id val id: String? = null,
)

private object AnalyticsDataCode {
    const val HOME = "home"
    const val GET_FEED = "get-feed"
    const val REQUEST_NEWSLETTER = "request-newsletter"
    const val RELEASE = "release"
    const val ERROR_GET_FEED = "error-get-feed"
    const val ERROR_PARSING = "error-parsing"
    const val ERROR_HOME = "error-home"
    const val ERROR_REQUEST_NEWSLETTER = "error-request-newsletter"
}

fun AnalyticEvent.toAnalyticsData(timestamp: Instant): AnalyticsData {
    return when (this) {
        is AnalyticEvent.GetFeed -> AnalyticsData(
            code = AnalyticsDataCode.GET_FEED,
            data = feedCode,
            timestamp = timestamp,
        )

        is AnalyticEvent.Home -> AnalyticsData(
            code = AnalyticsDataCode.HOME,
            timestamp = timestamp,
        )

        is AnalyticEvent.RequestNewsletter -> AnalyticsData(
            code = AnalyticsDataCode.REQUEST_NEWSLETTER,
            data = newsletterUrl,
            timestamp = timestamp,
        )

        is AnalyticEvent.Release -> AnalyticsData(
            code = AnalyticsDataCode.RELEASE,
            data = version,
            timestamp = timestamp,
        )

        is AnalyticEvent.Error.GetFeedError -> AnalyticsData(
            code = AnalyticsDataCode.ERROR_GET_FEED,
            data = feedCode,
            timestamp = timestamp,
        )

        is AnalyticEvent.Error.EmailParsingError -> AnalyticsData(
            code = AnalyticsDataCode.ERROR_PARSING,
            data = "${handlerName};;$emailTitle",
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
