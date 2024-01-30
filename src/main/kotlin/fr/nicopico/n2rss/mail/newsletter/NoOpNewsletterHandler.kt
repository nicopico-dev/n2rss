package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Article
import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Newsletter

/**
 * This [NewsletterHandler] will process all emails in the inbox folder.
 * Use this to ensure [fr.nicopico.n2rss.mail.client.EmailClient] is correctly configured
 */
@Suppress("unused")
class NoOpNewsletterHandler : NewsletterHandler {
    override val newsletter: Newsletter = Newsletter("NO-OP", "")

    override fun canHandle(email: Email): Boolean = true
    override fun extractArticles(email: Email): List<Article> = emptyList()
}
