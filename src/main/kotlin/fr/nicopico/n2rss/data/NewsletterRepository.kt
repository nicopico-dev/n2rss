package fr.nicopico.n2rss.data

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Newsletter
import org.springframework.stereotype.Component

@Component
class NewsletterRepository(
    private val handlers: List<NewsletterHandler>
) {
    fun findNewsletterByCode(code: String): Newsletter? {
        return handlers
            .map { it.newsletter }
            .firstOrNull { it.code == code }
    }
}
