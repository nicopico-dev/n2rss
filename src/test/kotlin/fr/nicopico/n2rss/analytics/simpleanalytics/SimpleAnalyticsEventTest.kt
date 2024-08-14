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

import fr.nicopico.n2rss.analytics.AnalyticsCode
import fr.nicopico.n2rss.analytics.AnalyticsEvent
import fr.nicopico.n2rss.config.N2RssProperties
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SimpleAnalyticsEventTest {

    @ParameterizedTest
    @MethodSource("provideEvents")
    fun `AnalyticEvent should convert to SimpleAnalyticsEvent`(
        event: AnalyticsEvent,
        analyticsProperties: N2RssProperties.SimpleAnalyticsProperties,
        expected: SimpleAnalyticsEvent,
    ) {
        val actual = event.toSimpleAnalyticsEvent(analyticsProperties)
        actual shouldBe expected
    }

    companion object {

        private const val UA = "user1"
        private const val HOSTNAME = "localhost"
        private val analyticsProperties = N2RssProperties.SimpleAnalyticsProperties(
            userAgent = UA,
            hostname = HOSTNAME,
        )

        private fun createSimpleAnalyticsEvent(
            event: String,
            metadata: Map<String, String> = emptyMap(),
            type: String = "event",
            hostname: String = HOSTNAME,
            ua: String = UA,
        ) = SimpleAnalyticsEvent(
            type = type,
            hostname = hostname,
            event = event,
            ua = ua,
            metadata = metadata,
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun provideEvents(): List<Arguments> = listOf(
            Arguments.of(
                AnalyticsEvent.Home(
                    userAgent = UA
                ),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_HOME
                ),
            ),
            Arguments.of(
                AnalyticsEvent.GetFeed(
                    feedCode = "feed1",
                    userAgent = UA
                ),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    // Feed code should be appended to the event,
                    // as metadata is not available for analysis in Simple Analytics
                    AnalyticsCode.EVENT_GET_FEED + "-feed1",
                    mapOf(
                        AnalyticsCode.DATA_FEED_CODE to "feed1"
                    )
                ),
            ),
            Arguments.of(
                AnalyticsEvent.RequestNewsletter(
                    newsletterUrl = "url1",
                    userAgent = UA
                ),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_REQUEST_NEWSLETTER,
                    mapOf(
                        AnalyticsCode.DATA_NEWSLETTER_URL to "url1"
                    )
                ),
            ),
            Arguments.of(
                AnalyticsEvent.NewRelease(version = "1.0.0"),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_RELEASE,
                    mapOf(
                        AnalyticsCode.DATA_VERSION to "1.0.0"
                    )
                ),
            ),
            Arguments.of(
                AnalyticsEvent.Error.HomeError,
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_ERROR_HOME,
                ),
            ),
            Arguments.of(
                AnalyticsEvent.Error.GetFeedError(feedCode = "feed1"),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_ERROR_GET_FEED,
                    mapOf(
                        AnalyticsCode.DATA_FEED_CODE to "feed1"
                    )
                ),
            ),
            Arguments.of(
                AnalyticsEvent.Error.EmailParsingError(
                    handlerName = "handler1",
                    emailTitle = "title1"
                ),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_ERROR_PARSING,
                    mapOf(
                        AnalyticsCode.DATA_HANDLER_NAME to "handler1",
                        AnalyticsCode.DATA_EMAIL_TITLE to "title1",
                    )
                ),
            ),
            Arguments.of(
                AnalyticsEvent.Error.RequestNewsletterError,
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_ERROR_REQUEST_NEWSLETTER,
                ),
            ),
            Arguments.of(
                AnalyticsEvent.GetRssFeeds(
                    userAgent = UA,
                ),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_GET_RSS_FEEDS
                ),
            ),
            Arguments.of(
                AnalyticsEvent.Error.GetRssFeedsError,
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    AnalyticsCode.EVENT_ERROR_GET_RSS_FEEDS
                ),
            )
        )
    }
}
