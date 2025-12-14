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
import fr.nicopico.n2rss.mail.models.html
import fr.nicopico.n2rss.newsletter.handlers.exception.NewsletterParsingException
import fr.nicopico.n2rss.newsletter.handlers.jsoup.indexOf
import fr.nicopico.n2rss.newsletter.handlers.jsoup.select
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class JetpackComposeAppDispatchNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "jetpack-compose-app-dispatch",
        name = "JetpackCompose.app's Dispatch",
        websiteUrl = "https://www.jetpackcompose.app"
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.startsWith("jetpackcomposeapp@")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.basic()
                .addTags("h1")
                .addAttributes("span", "style"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val linkElement = document.select("a[href]").first { it.text() == READ_ONLINE_LINK_TEXT }
        val linkUrl = linkElement.attr("href").toUrlOrNull()
            ?: throw NewsletterParsingException("Unable to find the \"Read Online\" link")

        val titleElements = document.select("h1")
            // The first H1 tag is the title of the newsletter
            .drop(1)

        val allElements = document.allElements
        return titleElements.map { titleElement ->
            val index = document.indexOf(titleElement)
            val nextIndex = document.select("h1", startingAfterIndex = index)
                ?.let { document.indexOf(it) }
                ?: allElements.size
            val articleDescription = allElements
                .subList(index + 1, nextIndex)
                .filter { it.tagName() == "p" }
                .joinToString(
                    separator = "\n\n",
                    transform = Element::text,
                )

            Article(
                title = titleElement.text(),
                link = linkUrl,
                description = articleDescription,
            )
        }
    }

    companion object {
        private const val READ_ONLINE_LINK_TEXT = "Read Online"
    }
}
