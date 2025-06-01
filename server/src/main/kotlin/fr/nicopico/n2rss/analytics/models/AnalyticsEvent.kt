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
package fr.nicopico.n2rss.analytics.models

sealed class AnalyticsEvent {
    /**
     * A user accessed the home page
     */
    data class Home(
        val userAgent: String,
    ) : AnalyticsEvent()

    /**
     * A user accessed the JSON list of RSS feeds
     */
    data class GetRssFeeds(
        val userAgent: String,
    ) : AnalyticsEvent()

    /**
     * A user accessed an RSS feed
     */
    data class GetFeed(
        val feedCode: String,
        val userAgent: String,
    ) : AnalyticsEvent()

    /**
     * A user requested support for a newsletter
     */
    data class RequestNewsletter(
        val newsletterUrl: String,
        val userAgent: String,
    ) : AnalyticsEvent()

    sealed class Error : AnalyticsEvent() {
        data object HomeError : Error()
        data object GetRssFeedsError : Error()
        data class GetFeedError(val feedCode: String) : Error()
        data object RequestNewsletterError : Error()
        data class EmailParsingError(
            val handlerName: String,
            val emailTitle: String
        ) : Error()
    }

    /**
     * A new version has been released to production
     */
    data class NewRelease(val version: String) : AnalyticsEvent()
}
