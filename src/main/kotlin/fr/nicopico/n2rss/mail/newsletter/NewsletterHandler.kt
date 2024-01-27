package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter
import fr.nicopico.n2rss.models.Publication

interface NewsletterHandler {
    val newsletter: Newsletter

    fun canHandle(email: Email): Boolean
    fun extractArticles(email: Email): List<Article>

    fun process(email: Email): Publication = Publication(
        title = email.subject,
        date = email.date,
        newsletter = newsletter,
        articles = extractArticles(email),
    )
}
