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
package fr.nicopico.n2rss.fakes

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandlerMultipleFeeds
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandlerSingleFeed
import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter

// Simulate constructor of a generic NewsletterHandlerFake
@Suppress("TestFunctionName")
fun NewsletterHandlerFake(vararg codes: String): NewsletterHandler {
    require(codes.isNotEmpty()) { "At least one code is required" }

    return if (codes.size == 1) {
        NewsletterHandlerSingleFeedFake(codes[0])
    } else {
        NewsletterHandlerMultipleFeedsFake(codes)
    }
}

// Simulate constructor of a generic NewsletterHandlerFake
@Suppress("TestFunctionName")
fun NewsletterHandlerFake(vararg newsletters: Newsletter): NewsletterHandler {
    require(newsletters.isNotEmpty()) { "At least one code is required" }

    return if (newsletters.size == 1) {
        NewsletterHandlerSingleFeedFake(newsletters[0])
    } else {
        NewsletterHandlerMultipleFeedsFake(newsletters.toList())
    }
}

private class NewsletterHandlerSingleFeedFake(
    override val newsletter: Newsletter,
) : NewsletterHandlerSingleFeed {

    constructor(code: String) : this(Newsletter(code, "Newsletter_$code", "Website_$code"))

    override fun canHandle(email: Email): Boolean {
        TODO("Not yet implemented")
    }

    override fun extractArticles(email: Email): List<Article> {
        TODO("Not yet implemented")
    }
}

private class NewsletterHandlerMultipleFeedsFake(
    override val newsletters: List<Newsletter>,
) : NewsletterHandlerMultipleFeeds {

    constructor(codes: Array<out String>)
        : this(codes.map { Newsletter(it, "Newsletter_$it", "Website_$it") })

    override fun canHandle(email: Email): Boolean {
        TODO("Not yet implemented")
    }

    override fun extractArticles(email: Email): Map<Newsletter, List<Article>> {
        TODO("Not yet implemented")
    }
}
