package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Email
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private val LOG = LoggerFactory.getLogger(EmailChecker::class.java)

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    private val newsletterHandlers: List<NewsletterHandler>,
    private val publicationRepository: PublicationRepository,
) {
    @Scheduled(
        initialDelay = 2,
        fixedRate = 3600,
        timeUnit = TimeUnit.SECONDS
    )
    fun savePublicationsFromEmails() {
        try {
            LOG.info("Checking emails...")
            val emails = emailClient.checkEmails()
            LOG.info("{} emails found, processing...", emails.size)

            val publications = emails
                .mapNotNull { email ->
                    try {
                        getNewsletterHandler(email)
                            ?.process(email)
                            ?.also {
                                emailClient.markAsRead(email)
                            }
                    } catch (e: Exception) {
                        LOG.error("Error processing email {}", email.subject, e)
                        null
                    }
                }

            if (publications.isNotEmpty()) {
                publicationRepository.saveAll(publications)
            }

            LOG.info("Processing done!")
        } catch (e: Exception) {
            LOG.error("Error while checking emails", e)
        }
    }

    private fun getNewsletterHandler(email: Email): NewsletterHandler? {
        return try {
            newsletterHandlers
                .single { it.canHandle(email) }
                .also { processor ->
                    LOG.info("\"{}\" is being processed by {}", email.subject, processor::class.java)
                }
        }  catch (_: NoSuchElementException) {
            LOG.warn("No handler found for email {}", email.subject)
            null
        } catch (_: IllegalArgumentException) {
            LOG.error("Too many handlers found for email {}", email.subject)
            null
        }
    }
}
