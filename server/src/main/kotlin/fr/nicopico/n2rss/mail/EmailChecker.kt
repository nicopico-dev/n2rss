/*
 * Copyright (c) 2025 Nicolas PICON
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

import fr.nicopico.n2rss.mail.client.EmailClient
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.handlers.exception.NoPublicationFoundException
import fr.nicopico.n2rss.newsletter.handlers.process
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.PublicationService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

private val LOG = LoggerFactory.getLogger(EmailChecker::class.java)

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    private val taskScheduler: TaskScheduler,
    private val newsletterService: NewsletterService,
    private val publicationService: PublicationService,
    private val monitoringService: MonitoringService,
    private val emailClientProperties: fr.nicopico.n2rss.config.N2RssProperties.EmailClientProperties,
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

            for (email in emails) {
                processEmail(email)
            }

            LOG.info("Processing done!")
        } catch (e: Exception) {
            LOG.error("Error while checking emails", e)
            monitoringService.notifyGenericError(e, context = "Checking emails")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun processEmail(email: Email) {
        try {
            val newsletterHandler = newsletterService.findNewsletterHandlerForEmail(email)
                ?: return

            LOG.info("\"{}\" is being processed by {}", email.subject, newsletterHandler::class.java)
            val publications = newsletterHandler.process(email)

            // At least one of the publications must have articles
            if (publications.all { it.articles.isEmpty() }) {
                throw NoPublicationFoundException()
            }

            publicationService.savePublications(publications)

            emailClient.markAsRead(email)
            if (emailClientProperties.moveAfterProcessingEnabled) {
                emailClient.moveToProcessed(email)
            }
        } catch (e: Exception) {
            LOG.error("Error processing email {}", email.subject, e)
            monitoringService.notifyEmailProcessingError(email, e)
        }
    }
}
