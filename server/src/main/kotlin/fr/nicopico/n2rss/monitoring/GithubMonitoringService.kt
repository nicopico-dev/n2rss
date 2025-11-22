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

package fr.nicopico.n2rss.monitoring

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.monitoring.data.GithubIssueData
import fr.nicopico.n2rss.monitoring.data.GithubIssueService
import fr.nicopico.n2rss.monitoring.github.GithubClient
import fr.nicopico.n2rss.monitoring.github.GithubException
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.newsletters
import fr.nicopico.n2rss.newsletter.models.Newsletter
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import kotlin.time.Clock

@Service
class GithubMonitoringService(
    private val service: GithubIssueService,
    private val client: GithubClient,
    private val clock: Clock,
) : MonitoringService {

    @Async
    @Transactional
    override fun notifyGenericError(error: Exception, context: String?) {
        val errorMessage = error.message ?: error.toString()
        try {
            val existing = service.findGenericError(errorMessage)
            if (existing == null) {
                val id = client.createIssue(
                    title = "An error occurred: `$errorMessage`",
                    body = "Context: ${context ?: "UNSPECIFIED"}\n"
                        + "Stacktrace:\n\n"
                        + "```\n"
                        + error.stackTraceToString()
                        + "```\n",
                    labels = listOf(
                        "n2rss-bot",
                        "generic-error",
                        "bug",
                    )
                )
                service.save(
                    GithubIssueData.GenericError(
                        issueId = id,
                        errorMessage = errorMessage,
                    )
                )
            }
        } catch (e: GithubException) {
            LOG.error("Unable to notify generic error", e)
        }
    }

    @Async
    @Transactional
    override fun notifyEmailProcessingError(email: Email, error: Exception, newsletterHandler: NewsletterHandler?) {
        val emailTitle = email.subject
        val errorMessage = error.message ?: error.toString()
        try {
            val existing = service.findEmailProcessingError(email, errorMessage)
            if (existing == null) {
                val newsletter = newsletterHandler?.newsletters?.first()
                val prefix = newsletter?.name?.let { "$it - " }
                    ?: ""

                val id = client.createIssue(
                    title = "${prefix}Email processing error on \"$emailTitle\"",
                    body = "Processing of email \"$emailTitle\" sent by \"${email.sender.sender}\" failed "
                        + "with the following error:\n\n"
                        + "```\n"
                        + error.stackTraceToString()
                        + "```\n",
                    labels = buildList {
                        add("n2rss-bot")
                        add("email-processing-error")
                        add("bug")

                        if (newsletter != null) {
                            add(newsletter.code)
                        }
                    }
                )
                service.save(
                    GithubIssueData.EmailProcessingError(
                        issueId = id,
                        emailTitle = emailTitle,
                        errorMessage = errorMessage,
                    )
                )
            } else {
                client.ensureIssueIsOpen(existing.issueId)
            }
        } catch (e: GithubException) {
            LOG.error("Unable to notify email processing error on email $emailTitle", e)
        }
    }

    @Async
    @Transactional
    override fun notifyNewsletterRequest(newsletterUrl: URL) {
        // Sanitize URL
        val uniqueUrl = URL(
            /* protocol = */ "https",
            /* host = */ newsletterUrl.host,
            /* port = */ newsletterUrl.port,
            /* file = */ "",
        )
        try {
            val today = getTodayString()
            val existing = service.findNewsletterRequest(uniqueUrl)
            if (existing == null) {
                val id = client.createIssue(
                    title = "Add support for newsletter \"$uniqueUrl\"",
                    body = "Initial request to support \"$uniqueUrl\" received on $today",
                    labels = listOf(
                        "n2rss-bot",
                        "newsletter-request"
                    )
                )
                service.save(GithubIssueData.NewsletterRequest(id, uniqueUrl))
            } else {
                val id = existing.issueId
                client.addCommentToIssue(issueId = id, body = "New request received on $today")
            }
        } catch (e: GithubException) {
            LOG.error("Unable to notify request for $uniqueUrl", e)
        }
    }

    @Async
    @Transactional
    override fun notifyMissingPublication(newsletter: Newsletter) {
        val existing = service.findMissingPublications(newsletter)
        if (existing == null) {
            val id = client.createIssue(
                title = "${newsletter.name} - Missing publications detected",
                body = "A new publication from ${newsletter.name} should have been received by now",
                labels = listOf(
                    "n2rss-bot",
                    "missing-publications",
                    newsletter.code,
                )
            )
            service.save(
                GithubIssueData.MissingPublications(
                    issueId = id,
                    newsletterCode = newsletter.code,
                )
            )
        } else {
            client.ensureIssueIsOpen(existing.issueId)
        }
    }

    private fun getTodayString(): String {
        return clock.now().format(
            DateTimeComponents.Format {
                year(Padding.ZERO)
                char('-')
                monthNumber(Padding.ZERO)
                char('-')
                day(Padding.ZERO)
            }
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GithubMonitoringService::class.java)
    }
}
