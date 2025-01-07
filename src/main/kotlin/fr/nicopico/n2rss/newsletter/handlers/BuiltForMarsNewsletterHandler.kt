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
import fr.nicopico.n2rss.newsletter.handlers.exception.NewsletterParsingException
import fr.nicopico.n2rss.newsletter.handlers.jsoup.indexOf
import fr.nicopico.n2rss.newsletter.handlers.jsoup.select
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component
import java.net.URL

@Component
class BuiltForMarsNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "builtformars",
        name = "Built for Mars",
        websiteUrl = "https://builtformars.com",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("peter@builtformars.com")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic()
                .addTags("table", "tr", "td")
                .addAttributes("tr", "id")
                .addAttributes("span", "id", "class"),
        )
        val cleanedDocument = Jsoup.parseBodyFragment(cleanedHtml)

        val singleArticleElement: Element? = cleanedDocument
            .selectFirst("tr#content-blocks")

        return if (singleArticleElement != null) {
            processSingleArticleDocument(
                title = email.subject.substringAfter(":").trim(),
                element = singleArticleElement
            )
        } else {
            processMultipleArticlesDocument(cleanedDocument)
        }.ifEmpty {
            processSingleArticleFallback(
                title = email.subject.substringAfter(":").trim(),
                document = cleanedDocument,
            )
        }
    }

    private fun processSingleArticleFallback(title: String, document: Document): List<Article> {
        val articleElement = document
            .selectFirst(":containsOwn(Hey \uD83D\uDC4B,)")
            ?.parents()
            ?.firstOrNull { it.`is`("table") }
            ?: throw NewsletterParsingException("Unable to find fallback article table")

        val link = articleElement.select("a[href]")
            .firstOrNull { it.text().isNotBlank() }
            ?.let { URL(it.attr("href")) }
            ?: throw NewsletterParsingException("Unable to find article link")

        val description = articleElement.select("td")
            .takeWhile { it.select("span").isNotEmpty() }
            .joinToString(separator = "\n\n") { it.text() }

        return listOf(
            Article(
                title = title,
                link = link,
                description = description,
            )
        )
    }

    private fun processSingleArticleDocument(title: String, element: Element): List<Article> {
        val document = Jsoup.clean(
            element
                .html()
                ?: throw NewsletterParsingException("Unable to find content-blocks"),
            Safelist.basic()
        ).let {
            Jsoup.parseBodyFragment(it)
        }

        val link = document.select("a[href]")
            .firstOrNull { it.text().isNotBlank() }
            ?.let { URL(it.attr("href")) }
            ?: throw NewsletterParsingException("Unable to find article link")
        val description = document.select("p")
            .takeWhile { it.select("span").isEmpty() }
            .joinToString(separator = "\n\n") { it.text() }

        return listOf(
            Article(
                title = title,
                link = link,
                description = description,
            )
        )
    }

    private fun processMultipleArticlesDocument(document: Document): List<Article> {
        return document.select("span.bold")
            .filter { titleElement ->
                MULTIPLE_ARTICLES_PREFIX_REGEX.containsMatchIn(titleElement.text())
            }
            .map { titleElement ->
                val title = titleElement.text()
                    .replace(MULTIPLE_ARTICLES_PREFIX_REGEX, "")

                val titleElementIndex = document.indexOf(titleElement)
                val description = document.select("td", startingAfterIndex = titleElementIndex)?.text()
                    ?: throw NewsletterParsingException("Unable to find description for article \"$title\"")

                val linkElement = document.select("a[href]", startingAfterIndex = titleElementIndex)
                    ?: throw NewsletterParsingException("Unable to find link for article \"$title\"")
                val link = URL(linkElement.attr("href"))

                Article(
                    title = title,
                    link = link,
                    description = description,
                )
            }
    }

    companion object {
        private val MULTIPLE_ARTICLES_PREFIX_REGEX = Regex("\\d+\\.\\s+")
    }
}
