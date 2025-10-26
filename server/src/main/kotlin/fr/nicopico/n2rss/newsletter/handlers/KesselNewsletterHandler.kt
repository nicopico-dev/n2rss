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
import fr.nicopico.n2rss.newsletter.handlers.jsoup.backgroundColor
import fr.nicopico.n2rss.newsletter.handlers.jsoup.findElementAfter
import fr.nicopico.n2rss.newsletter.handlers.jsoup.textWithLineFeeds
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component
import java.net.URL

@Component
class KesselNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "kessel",
        name = "Kessel",
        websiteUrl = "https://kessel.media",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("kessel.media")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("h2", "p", "span", "a", "br")
                .addAttributes("a", "href")
                .addAttributes("p", "style")
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        val articleTitles = document.select("h2")
        return articleTitles.flatMap { it.extractArticles(document) }
    }

    private fun Element.extractArticles(fullDocument: Document): List<Article> {
        val mainTitle = this.text()
        val subElements = nextElementSiblings()
            .takeWhile { it.tagName() != "h2" }

        val subTitleElements = subElements.filter {
            it.`is`("p[style]") && it.backgroundColor == "#003fff"
        }

        return if (subTitleElements.isNotEmpty()) {
            subTitleElements.mapNotNull { titleElement ->
                val link = fullDocument.findFirstLinkAfter(
                    element = titleElement,
                    cssSelector = "a[href]",
                ) ?: return@mapNotNull null

                val description = fullDocument.findElementAfter("p", titleElement)?.text()
                    ?: throw NewsletterParsingException("Article description not found for `$titleElement`")

                Article(
                    title = titleElement.text(),
                    link = link,
                    description = description,
                )
            }
        } else {
            val link = fullDocument.findFirstLinkAfter(
                element = this,
                cssSelector = "a[href]:contains(${MAIN_ARTICLE_LINK_LABEL})"
            ) ?: return emptyList()

            listOf(
                Article(
                    title = mainTitle,
                    link = link,
                    description = subElements.joinToString(
                        separator = "\n\n",
                        transform = {
                            it.textWithLineFeeds()
                        }
                    ),
                )
            )
        }
    }

    private fun Document.findFirstLinkAfter(
        element: Element,
        cssSelector: String,
    ): URL? {
        val linkElement = findElementAfter(cssSelector, element)
        return linkElement?.attr("href")?.toUrlOrNull()
    }

    companion object {
        private const val MAIN_ARTICLE_LINK_LABEL = "Lire l’article"
    }
}
