package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private val LOG = LoggerFactory.getLogger(EmailChecker::class.java)

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    private val newsletterHandlers: List<NewsletterHandler>,
) {
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    fun checkEmails() {
        LOG.info("Checking emails...")
        val emails = emailClient.checkEmails()
        LOG.info("{} emails found, processing...", emails.size)

        for (email in emails) {
            for (processor in newsletterHandlers) {
                if (processor.canHandle(email)) {
                    LOG.debug("\"{}\" is being processed by {}", email.subject, processor::class.java)
                    processor.process(email)
                }
            }
        }
        LOG.info("Processing done!")
    }
}
