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
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import java.util.Locale

class GDIYNewsletterHandler : NewsletterHandlerSingleFeed {

    override val newsletter: Newsletter = Newsletter(
        code = "gdiy",
        name = "Génération Do It Yourself",
        websiteUrl = "https://www.gdiy.fr/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email == "contact@gdiy.fr"
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.none()
                .addTags("a", "span", "p")
                .addAttributes("a", "href")
                .addAttributes("span", "style")
        )
        // TODO println(Jsoup.parseBodyFragment(email.content.html))
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        // TODO println(document)

        return document
            .select("a[href] > span[style*=color]")
            .filter { element ->
                element.matchColor("#257953")
                    && element.text().matches(ARTICLE_TITLE_REGEX)
            }
            .map { linkSpan ->
                val link = requireNotNull(linkSpan.parent())
                val description = link.nextElementSiblings()
                    .takeWhile { element -> element.tagName() == "p" }
                    .joinToString("\n\n") { it.text() }
                    .trim()

                Article(
                    title = link.text(),
                    link = link.attr("href").toUrlOrNull()
                        ?: throw NewsletterParsingException("No valid link for article"),
                    description = description,
                )
            }
    }

    private fun Element.matchColor(hexColor: String): Boolean {
        val extractedColor = extractCssColor() ?: return false
        return extractedColor.equals(hexColor, ignoreCase = true)
    }

    private fun Element.extractCssColor(): String? {
        val style = attr("style")
        val hexColorRegex = Regex("color\\s*:\\s*(#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{3})\\b")
        val rgbColorRegex = Regex("color\\s*:\\s*rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)")

        return hexColorRegex.find(style)?.groupValues?.get(1)
            ?: rgbColorRegex.find(style)?.let {
                val (r, g, b) = it.destructured
                String.format(Locale.ROOT, "#%02X%02X%02X", r.toInt(), g.toInt(), b.toInt())
            }
    }

    companion object {
        private val ARTICLE_TITLE_REGEX = Regex("#\\d+ - .*")
    }
}
