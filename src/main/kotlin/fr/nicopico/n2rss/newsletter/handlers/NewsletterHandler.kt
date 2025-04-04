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
package fr.nicopico.n2rss.newsletter.handlers

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication

sealed interface NewsletterHandler {
    fun canHandle(email: Email): Boolean
}

val NewsletterHandler.newsletters: List<Newsletter>
    get() = when (this) {
        is NewsletterHandlerSingleFeed -> listOf(newsletter)
        is NewsletterHandlerMultipleFeeds -> newsletters
    }

fun NewsletterHandler.process(email: Email): List<Publication> {
    return when (this) {
        is NewsletterHandlerSingleFeed -> listOf(process(email))
        is NewsletterHandlerMultipleFeeds -> process(email)
    }
}

interface NewsletterHandlerSingleFeed : NewsletterHandler {
    val newsletter: Newsletter

    fun extractArticles(email: Email): List<Article>

    fun process(email: Email): Publication = Publication(
        title = email.subject,
        date = email.date,
        newsletter = newsletter,
        articles = extractArticles(email),
    )
}

interface NewsletterHandlerMultipleFeeds : NewsletterHandler {
    val newsletters: List<Newsletter>

    fun extractArticles(email: Email): Map<Newsletter, List<Article>>

    fun process(email: Email): List<Publication> {
        return extractArticles(email)
            .map { (newsletter, articles) ->
                Publication(
                    title = email.subject,
                    date = email.date,
                    newsletter = newsletter,
                    articles = articles,
                )
            }
    }
}
