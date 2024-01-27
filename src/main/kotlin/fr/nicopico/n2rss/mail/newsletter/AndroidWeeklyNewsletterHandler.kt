package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Entry
import fr.nicopico.n2rss.models.EntrySource
import fr.nicopico.n2rss.utils.toURL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

class AndroidWeeklyNewsletterHandler : NewsletterHandler {
    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("contact@androidweekly.net")
    }

    override fun process(email: Email): List<Entry> {
        // Use style instead of markers -> font-size, color, etc.
        // Delimit by "Articles & Tutorials" and "Place a sponsored post"
        // or the blue rectangle for section titles

        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic()
                .addAttributes("span", "style"),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        val start: Element = document.select("span").first { it.ownText() == "Articles & Tutorials" }
        val sectionTitleStyle = start.attr("style")
        val end: Element = start.nextElementSiblings().first { it.attr("style") == sectionTitleStyle }

        val articleSectionDocument = Document("").apply {
            val nodesBetween = (start.parent()?.childNodes() ?: emptyList())
                .dropWhile { it != start }
                .takeWhile { it != end }
            appendChildren(nodesBetween)
        }

        val entrySource = EntrySource(
            handler = this::class,
            title = email.subject,
        )

        val entries = articleSectionDocument.select("a[href]")
            .filter { it -> it.text().isNotBlank() }
            .mapNotNull { tag ->
                // Ignore entries with invalid link
                tag.attr("href").toURL()
                    ?.let { link ->
                        Entry(
                            title = tag.text().trim(),
                            link = link,
                            description = tag.nextSibling().toString().trim(),
                            pubDate = email.date,
                            source = entrySource,
                        )
                    }
            }

        return entries
    }
}
