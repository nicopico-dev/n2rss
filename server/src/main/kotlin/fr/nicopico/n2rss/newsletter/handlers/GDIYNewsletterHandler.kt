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
import fr.nicopico.n2rss.newsletter.handlers.jsoup.HtmlColor
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class GDIYNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "gdiy",
        name = "Génération Do It Yourself",
        websiteUrl = "https://www.gdiy.fr/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email == "contact@gdiy.fr"
            && email.replyTo == email.sender
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("a", "span", "p")
                .addAttributes("a", "href")
                .addAttributes("span", "style")
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        return document
            .select("a[href] > span[style*=color]")
            .filter { element ->
                element.getTextColor()?.matches(ARTICLE_TITLE_COLOR, tolerance = 5) == true
                    && element.text().matches(ARTICLE_TITLE_REGEX)
            }
            .map { linkSpan ->
                val link = requireNotNull(linkSpan.parent())
                val description = link.nextElementSiblings()
                    .takeWhile { element -> element.tagName() == "p" }
                    .joinToString("\n\n") { it.text() }
                    .trim()

                Article(
                    title = ARTICLE_TITLE_REGEX.matchEntire(link.text())!!.groupValues[1],
                    link = link.attr("href").toUrlOrNull()
                        ?: throw NewsletterParsingException("No valid link for article"),
                    description = description,
                )
            }
    }

    private fun Element.getTextColor(): HtmlColor? {
        val style = attr("style")
        return HtmlColor.extractFromStyle(style)
    }

    companion object {
        private val ARTICLE_TITLE_COLOR = HtmlColor.of("#257953")
        private val ARTICLE_TITLE_REGEX = Regex(".*(#\\d+ - .*)")
    }
}
