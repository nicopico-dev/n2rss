package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.toURL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

class PointerNewsletterHandler : NewsletterHandler {
    override val newsletter: Newsletter = Newsletter(
        "Pointer",
        "http://www.pointer.io/",
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
        val sponsorLink = sponsorSubtitleElement?.attr("href")?.toURL()

        return if (sponsorSubtitleElement != null && sponsorLink != null) {
            val sponsorName = sponsorSection.select("p")
                .map { it.text() }
                .firstOrNull(String::isNotEmpty)
                ?.let {
                    it.substring(it.indexOf("is presented by") + 15)
                }
                ?.trim()
                ?: "?"

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
            val link = articleTitle.attr("href").toURL()
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
        }

}
