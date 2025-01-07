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

object AnalyticsCode {
    const val EVENT_REQUEST_NEWSLETTER = "request-newsletter"
    const val EVENT_RELEASE = "release"
    const val EVENT_ERROR_GET_FEED = "error-get-feed"
    const val EVENT_ERROR_GET_RSS_FEEDS = "error-get-rss-feeds"
    const val EVENT_ERROR_PARSING = "error-parsing"
    const val EVENT_ERROR_HOME = "error-home"
    const val EVENT_ERROR_REQUEST_NEWSLETTER = "error-request-newsletter"

    const val DATA_FEED_CODE = "feedCode"
    const val DATA_USER_AGENT = "userAgent"
    const val DATA_NEWSLETTER_URL = "newsletterUrl"
    const val DATA_VERSION = "version"
    const val DATA_HANDLER_NAME = "handlerName"
    const val DATA_EMAIL_TITLE = "emailTitle"
}
