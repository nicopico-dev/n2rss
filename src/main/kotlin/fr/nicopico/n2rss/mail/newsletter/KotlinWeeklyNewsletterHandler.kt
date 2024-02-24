package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

class KotlinWeeklyNewsletterHandler : NewsletterHandler {

    override val newsletter: Newsletter = Newsletter(
        code = "kotlin_weekly",
        name = "Kotlin Weekly",
        websiteUrl = "http://kotlinweekly.net/",
    )

    override fun canHandle(email: Email): Boolean {
        return email.sender.email.contains("mailinglist@kotlinweekly.net")
    }

    override fun extractArticles(email: Email): List<Article> {
        val cleanedHtml = Jsoup.clean(
            email.content,
            Safelist.basic(),
        )
        val document = Jsoup.parseBodyFragment(cleanedHtml)
        println(cleanedHtml)
        return emptyList()
    }
}
