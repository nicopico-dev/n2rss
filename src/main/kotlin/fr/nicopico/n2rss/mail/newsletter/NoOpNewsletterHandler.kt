package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Entry
import org.springframework.stereotype.Component
import java.net.URL

/**
 * This [NewsletterHandler] will process all emails in the inbox folder.
 * Use this to ensure [fr.nicopico.n2rss.mail.client.EmailClient] is correctly configured
 */
@Component
class NoOpNewsletterHandler : NewsletterHandler {
    override fun canHandle(email: Email): Boolean = true

    override fun process(email: Email): Entry {
        return Entry(
            source = NoOpNewsletterHandler::class,
            title = email.subject,
            preview = email.content.substring(0, minOf(email.content.length, 100)),
            url = URL("https://example.com")
        )
    }
}
