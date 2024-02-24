package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.utils.toURL
import org.jsoup.Jsoup
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
            email.content,
            Safelist.basic(),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        val links = document.select("a[href]:has(strong:has(span))")
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
}
