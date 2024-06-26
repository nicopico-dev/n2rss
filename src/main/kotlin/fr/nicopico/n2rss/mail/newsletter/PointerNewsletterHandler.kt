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
package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.url.toUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component

@Component
class PointerNewsletterHandler : NewsletterHandlerSingleFeed {
    override val newsletter: Newsletter = Newsletter(
        code = "pointer",
        name = "Pointer",
        websiteUrl = "http://www.pointer.io/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("suraj@pointer.io")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content.preserveSeparators(),
            Safelist.basic()
                .addAttributes("p", "style"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)

        // Separator have been changed from <td> tags to <p> tags
        // by `String.preserveSeparators()` extension method
        val firstSeparator: Element = document.select("p[style]")
            .first { it.isSeparator }

        val sponsor = findSponsor(firstSeparator)
        val articles = findArticles(firstSeparator)

        return (sponsor?.let(::listOf) ?: emptyList()) + articles
    }

    private fun findSponsor(firstSeparator: Element): Article? {
        val sponsorSection = Document("")
            .apply {
                appendChildren(
                    (firstSeparator.parent()?.childNodes() ?: emptyList())
                        .takeWhile { it != firstSeparator }
                )
            }

        val sponsorSubtitleElement = sponsorSection.selectFirst("a[href]:has(strong:has(span))")
        val sponsorLink = sponsorSubtitleElement?.attr("href")?.toUrlOrNull()

        return if (sponsorSubtitleElement != null && sponsorLink != null) {
            val sponsorName = sponsorSection.select("p")
                .map { it.text() }
                .firstOrNull(String::isNotEmpty)
                ?.let {
                    val sponsorPrefix = "is presented by"
                    it.substring(it.indexOf(sponsorPrefix) + sponsorPrefix.length)
                }
                ?.trim()
                ?: throw NewsletterParsingException("Cannot find sponsor name in $sponsorSection")

            val sponsorSubtitle = sponsorSubtitleElement.text()
            val sponsorDescription = sponsorSection.text().let {
                it.substring(it.indexOf(sponsorSubtitle) + sponsorSubtitle.length).trim()
            }

            Article(
                title = "SPONSOR - $sponsorName: $sponsorSubtitle",
                link = sponsorLink,
                description = sponsorDescription,
            )
        } else null
    }

    private fun findArticles(firstSeparator: Element): List<Article> {
        // Take articles after the first separator to ignore the sponsor
        val articleSectionDocument = Document("")
            .apply {
                appendChildren(
                    (firstSeparator.parent()?.childNodes() ?: emptyList())
                        .dropWhile { it != firstSeparator }
                )
            }

        val links = articleSectionDocument.select("a[href]:has(strong:has(span))")
        val articles = links.mapNotNull { articleTitle ->
            val link = articleTitle.attr("href").toUrlOrNull()
                ?: return@mapNotNull null
            val title = articleTitle.text()
            val description = articleTitle.findDescription()

            Article(
                title = title,
                link = link,
                description = description
            )
        }
        return articles
    }

    private fun Element.findDescription(): String {
        val descriptionElement = parent()
            ?.nextElementSiblings()
            ?.select("p:has(strong:contains(tl;dr))")
            ?.first()
        return descriptionElement?.text()?.removePrefix("tl;dr:")?.trim()
            ?: "N/A"
    }

    /**
     * Original separator are <td> tags with a specific style, but these tags cannot be kept without
     * the whole <table> structure. This function replace <td> with <p> tags
     */
    private fun String.preserveSeparators(): String {
        val doc = Jsoup.parse(this)
        doc.select("td[style]")
            .filter { element -> element.isSeparator }
            .forEach { td ->
                val pElement = Element("p")
                    .attr("style", td.attr("style"))
                    .html(td.html())
                td.replaceWith(pElement)
            }
        return doc.html()
    }

    /**
     * Check if the element has the style of a separator
     * "min-width:100%;border-top:2px solid #000000"
     */
    private val Element.isSeparator: Boolean
        get() {
            val style = attr("style")
            return style.contains(Regex("border-top\\s*:\\s*2px\\s*solid\\s*#000000"))
                || (
                style.contains(Regex("border-top-width:\\s*2px\\b"))
                    && style.contains(Regex("border-top-style:\\s*solid\\b"))
                    && style.contains(Regex("border-top-color:\\s*#000000\\b"))
                )
        }
}
