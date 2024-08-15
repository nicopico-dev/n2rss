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

import fr.nicopico.n2rss.analytics.AnalyticsEvent
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AnalyticsDataTest {

    @ParameterizedTest
    @MethodSource("provideEvents")
    fun `AnalyticEvent should convert to AnalyticsData`(
        event: AnalyticsEvent, expected: AnalyticsData,
    ) {
        val actual = event.toAnalyticsData(timestamp)
        actual shouldBe expected
    }

    companion object {
        val timestamp = Clock.System.now()

        private const val UA = "user1"
        private const val UA_FINGERPRINT = "0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90"

        @JvmStatic
        @Suppress("LongMethod")
        fun provideEvents(): List<Arguments> = listOf(
            Arguments.of(
                AnalyticsEvent.Home(
                    userAgent = UA
                ),
                AnalyticsData(
                    code = "home",
                    data = mapOf("userAgent" to UA_FINGERPRINT),
                    timestamp = timestamp,
                )
            ),
            Arguments.of(
                AnalyticsEvent.GetFeed(
                    feedCode = "feed1",
                    userAgent = UA
                ),
                AnalyticsData(
                    code = "get-feed",
                    timestamp = timestamp,
                    data = mapOf("feedCode" to "feed1", "userAgent" to UA_FINGERPRINT)
                )
            ),
            Arguments.of(
                AnalyticsEvent.RequestNewsletter(
                    newsletterUrl = "url1",
                    userAgent = UA
                ),
                AnalyticsData(
                    code = "request-newsletter",
                    timestamp = timestamp,
                    data = mapOf("newsletterUrl" to "url1", "userAgent" to UA_FINGERPRINT)
                )
            ),
            Arguments.of(
                AnalyticsEvent.NewRelease(version = "1.0.0"),
                AnalyticsData(
                    code = "release",
                    timestamp = timestamp,
                    data = mapOf("version" to "1.0.0")
                )
            ),
            Arguments.of(
                AnalyticsEvent.Error.HomeError,
                AnalyticsData(
                    code = "error-home",
                    timestamp = timestamp
                )
            ),
            Arguments.of(
                AnalyticsEvent.Error.GetFeedError(feedCode = "feed1"),
                AnalyticsData(
                    code = "error-get-feed",
                    timestamp = timestamp,
                    data = mapOf("feedCode" to "feed1")
                )
            ),
            Arguments.of(
                AnalyticsEvent.Error.EmailParsingError(
                    handlerName = "handler1",
                    emailTitle = "title1"
                ),
                AnalyticsData(
                    code = "error-parsing",
                    timestamp = timestamp,
                    data = mapOf("handlerName" to "handler1", "emailTitle" to "title1")
                )
            ),
            Arguments.of(
                AnalyticsEvent.Error.RequestNewsletterError,
                AnalyticsData(
                    code = "error-request-newsletter",
                    timestamp = timestamp
                )
            ),
            Arguments.of(
                AnalyticsEvent.GetRssFeeds(
                    userAgent = UA,
                ),
                AnalyticsData(
                    code = "get-rss-feeds",
                    data = mapOf("userAgent" to UA_FINGERPRINT),
                    timestamp = timestamp,
                )
            ),
            Arguments.of(
                AnalyticsEvent.Error.GetRssFeedsError,
                AnalyticsData(
                    code = "error-get-rss-feeds",
                    timestamp = timestamp,
                )
            )
        )
    }

}
