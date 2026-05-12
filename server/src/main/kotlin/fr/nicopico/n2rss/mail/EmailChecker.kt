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
import fr.nicopico.n2rss.mail.client.EmailClientSession
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.monitoring.MonitoringService
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.exception.NoPublicationFoundException
import fr.nicopico.n2rss.newsletter.handlers.newsletters
import fr.nicopico.n2rss.newsletter.handlers.process
import fr.nicopico.n2rss.newsletter.service.NewsletterService
import fr.nicopico.n2rss.newsletter.service.PublicationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val LOG = LoggerFactory.getLogger(EmailChecker::class.java)

@Component
class EmailChecker(
    private val emailClient: EmailClient,
    private val newsletterService: NewsletterService,
    private val publicationService: PublicationService,
    private val monitoringService: MonitoringService,
    @param:Value($$"${n2rss.email.client.move-after-processing-enabled:false}")
    private val moveAfterProcessingEnabled: Boolean,
) {
    // We want to catch all exceptions here
    @Suppress("TooGenericExceptionCaught")
    @Scheduled(cron = "\${n2rss.email.cron}")
    fun savePublicationsFromEmails() {
        try {
            LOG.info("Checking emails...")
            emailClient.openSession().use { session ->
                checkEmails(session)
            }
        } catch (e: Exception) {
            LOG.error("Error while checking emails", e)
            monitoringService.notifyGenericError(e, context = "Checking emails")
        }
    }

    private fun checkEmails(session: EmailClientSession) {
        val emails = session.checkEmails()
        LOG.info("{} emails found, processing...", emails.size)

        val processedEmails = mutableListOf<Email>()

        for (email in emails) {
            if (processEmail(session, email)) {
                processedEmails.add(email)
            }
        }

        LOG.info("Processing done!")

        if (moveAfterProcessingEnabled && processedEmails.isNotEmpty()) {
            LOG.debug("Move processed emails to specified folder")
            moveAllProcessedEmails(session, processedEmails)
        }
    }

    @Suppress("TooGenericExceptionCaught", "NestedBlockDepth", "ReturnCount")
    private fun processEmail(session: EmailClientSession, email: Email): Boolean {
        var newsletterHandler: NewsletterHandler? = null
        try {
            newsletterHandler = newsletterService.findNewsletterHandlerForEmail(email)
                ?: return false

            val publicationAlreadySaved = publicationService.doesPublicationAlreadyExist(
                title = email.subject,
                newsletters = newsletterHandler.newsletters,
            )

            if (publicationAlreadySaved) {
                LOG.warn("\"{}\" ignored, publication already exists!", email.subject)
            } else {
                LOG.info("\"{}\" is being processed by {}", email.subject, newsletterHandler::class.java)
                val publications = newsletterHandler.process(email)

                // At least one of the publications must have articles
                if (publications.all { it.articles.isEmpty() }) {
                    throw NoPublicationFoundException()
                }

                publicationService.savePublications(publications)
            }

            try {
                LOG.info("\"{}\" processing done, marking email as read", email.subject)
                session.markAsRead(email)
                return true
            } catch (e: Exception) {
                LOG.error("Error while marking email {} as processed", email.subject, e)
                monitoringService.notifyGenericError(e, context = "Marking email '${email.subject}' as processed")
            }
        } catch (e: Exception) {
            LOG.error("Error processing email {}", email.subject, e)
            monitoringService.notifyEmailProcessingError(email, e, newsletterHandler)
        }

        return false
    }

    @Suppress("TooGenericExceptionCaught")
    private fun moveAllProcessedEmails(session: EmailClientSession, processedEmails: List<Email>) {
        try {
            session.moveToProcessed(processedEmails)
        } catch (e: Exception) {
            LOG.warn("Error while moving processed emails to specified folder", e)
            monitoringService.notifyGenericError(e, context = "Moving emails to specified folder")
        }
    }
}
