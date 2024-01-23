package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    private val newsletterHandlers: List<NewsletterHandler>,
) {
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    fun checkEmails() {
        val emails = emailClient.checkEmails()
        for (email in emails) {
            for (processor in newsletterHandlers) {
                if (processor.canHandle(email)) {
                    processor.process(email)
                }
            }
        }
    }
}
