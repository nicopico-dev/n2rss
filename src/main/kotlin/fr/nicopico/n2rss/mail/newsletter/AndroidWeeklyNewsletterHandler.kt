package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.toURL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

class AndroidWeeklyNewsletterHandler : NewsletterHandler {

    override val newsletter: Newsletter = Newsletter(
        name = "Android Weekly",
        websiteUrl = "https://androidweekly.net"
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("contact@androidweekly.net")
    }

    override fun extractArticles(email: Email): List<Article> {
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

        return articleSectionDocument.select("a[href]")
            .filter { it -> it.text().isNotBlank() }
            .mapNotNull { tag ->
                // Ignore entries with invalid link
                tag.attr("href").toURL()
                    ?.let { link ->
                        Article(
                            title = tag.text().trim(),
                            link = link,
                            description = tag.nextSibling().toString().trim(),
                        )
                    }
            }
    }
}
