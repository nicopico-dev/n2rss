/*
 * Copyright (c) 2024 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fr.nicopico.n2rss.mail

import fr.nicopico.n2rss.config.NewsletterConfiguration
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.Email
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

private val LOG = LoggerFactory.getLogger(EmailChecker::class.java)

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    @Qualifier(NewsletterConfiguration.ENABLED_NEWSLETTER_HANDLERS)
    private val newsletterHandlers: List<NewsletterHandler>,
    private val publicationRepository: PublicationRepository,
    private val taskScheduler: TaskScheduler,
) {
    @PostConstruct
    fun checkEmailsOnStart() {
        taskScheduler.schedule(
            /* task = */ this::savePublicationsFromEmails,
            /* startTime = */ Instant.now().plusSeconds(2)
        )
    }

    // We want to catch all exceptions here
    @Suppress("TooGenericExceptionCaught")
    @Scheduled(cron = "\${n2rss.email.cron}")
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
        } catch (_: NoSuchElementException) {
            LOG.warn("No handler found for email {}", email.subject)
            null
        } catch (_: IllegalArgumentException) {
            LOG.error("Too many handlers found for email {}", email.subject)
            null
        }
    }
}
