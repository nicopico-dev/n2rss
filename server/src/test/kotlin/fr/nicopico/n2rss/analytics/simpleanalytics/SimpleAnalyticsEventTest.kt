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

package fr.nicopico.n2rss.analytics.simpleanalytics

import fr.nicopico.n2rss.analytics.models.AnalyticsCode
import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.service.simpleanalytics.SimpleAnalyticsEvent
import fr.nicopico.n2rss.analytics.service.simpleanalytics.toSimpleAnalyticsEvent
import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.utils.getFingerprint
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
        expected: SimpleAnalyticsEvent?,
    ) {
        val actual = event.toSimpleAnalyticsEvent(analyticsProperties)
        actual shouldBe expected
    }

    companion object {

        private const val HOSTNAME = "localhost"
        private const val SERVER_UA = "server1"
        private const val CLIENT_UA = "user1"

        private val analyticsProperties = N2RssProperties.SimpleAnalyticsProperties(
            userAgent = SERVER_UA,
            hostname = HOSTNAME,
        )

        private val CLIENT_UA_FINGERPRINT = requireNotNull(getFingerprint(CLIENT_UA))

        @Suppress("SameParameterValue")
        private fun createSimpleAnalyticsEvent(
            event: String,
            metadata: Map<String, String> = emptyMap(),
            hostname: String,
            ua: String,
        ) = SimpleAnalyticsEvent(
            type = "event",
            hostname = hostname,
            event = event,
            ua = ua,
            metadata = metadata,
        )

        @Suppress("SameParameterValue")
        private fun createSimpleAnalyticsPageView(
            path: String,
            hostname: String,
            ua: String,
        ) = SimpleAnalyticsEvent(
            type = "pageview",
            event = "pageview",
            path = path,
            hostname = hostname,
            ua = ua,
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun provideEvents(): List<Arguments> = listOf(
            Arguments.of(
                AnalyticsEvent.Home(
                    userAgent = CLIENT_UA,
                ),
                analyticsProperties,
                null,   // Home does not send any event or page-view
            ),

            //region Page views
            Arguments.of(
                AnalyticsEvent.GetRssFeeds(
                    userAgent = CLIENT_UA,
                ),
                analyticsProperties,
                createSimpleAnalyticsPageView(
                    path = "/rss",
                    hostname = HOSTNAME,
                    ua = CLIENT_UA,
                ),
            ),

            Arguments.of(
                AnalyticsEvent.GetFeed(
                    feedCode = "feed1",
                    userAgent = CLIENT_UA,
                ),
                analyticsProperties,
                createSimpleAnalyticsPageView(
                    path = "/rss/feed1",
                    hostname = HOSTNAME,
                    ua = CLIENT_UA,
                ),
            ),
            //endregion

            //region Events
            Arguments.of(
                AnalyticsEvent.RequestNewsletter(
                    newsletterUrl = "url1",
                    userAgent = CLIENT_UA,
                ),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    event = AnalyticsCode.EVENT_REQUEST_NEWSLETTER,
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                    metadata = mapOf(
                        AnalyticsCode.DATA_NEWSLETTER_URL to "url1",
                        AnalyticsCode.DATA_USER_AGENT to CLIENT_UA_FINGERPRINT,
                    ),
                ),
            ),

            Arguments.of(
                AnalyticsEvent.NewRelease(version = "1.0.0"),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    event = AnalyticsCode.EVENT_RELEASE,
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                    metadata = mapOf(
                        AnalyticsCode.DATA_VERSION to "1.0.0"
                    )
                ),
            ),
            //endregion

            //region Errors
            Arguments.of(
                AnalyticsEvent.Error.HomeError,
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    event = AnalyticsCode.EVENT_ERROR_HOME,
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                ),
            ),
            Arguments.of(
                AnalyticsEvent.Error.GetRssFeedsError,
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    event = AnalyticsCode.EVENT_ERROR_GET_RSS_FEEDS,
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                ),
            ),

            Arguments.of(
                AnalyticsEvent.Error.GetFeedError(feedCode = "feed1"),
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    event = AnalyticsCode.EVENT_ERROR_GET_FEED,
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                    metadata = mapOf(
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
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                    metadata = mapOf(
                        AnalyticsCode.DATA_HANDLER_NAME to "handler1",
                        AnalyticsCode.DATA_EMAIL_TITLE to "title1",
                    ),
                ),
            ),

            Arguments.of(
                AnalyticsEvent.Error.RequestNewsletterError,
                analyticsProperties,
                createSimpleAnalyticsEvent(
                    event = AnalyticsCode.EVENT_ERROR_REQUEST_NEWSLETTER,
                    hostname = HOSTNAME,
                    ua = SERVER_UA,
                ),
            ),
            //endregion
        )
    }
}
