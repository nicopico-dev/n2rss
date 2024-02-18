package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.toURL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

class PointerNewsletterHandler() : NewsletterHandler {
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

        // Separator have been changed from <td> tags to <p> tags by `String.preserveSeparators()` extension method
        val separator: Element = document.select("p[style]")
            .first { it.isSeparator }

        // Take articles after the first separator to ignore the sponsor
        val articleSectionDocument = Document("")
            .apply {
                appendChildren(
                    (separator.parent()?.childNodes() ?: emptyList())
                        .dropWhile { it != separator }
                )
            }

        val links = articleSectionDocument.select("a[href]:has(strong:has(span))")
        return links.mapNotNull { tag ->
            tag.attr("href").toURL()
                ?.let { link ->
                    Article(
                        title = tag.text(),
                        link = link,
                        description = "TODO"
                    )
                }
        }
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
