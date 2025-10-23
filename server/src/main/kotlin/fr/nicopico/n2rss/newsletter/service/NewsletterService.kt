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
package fr.nicopico.n2rss.newsletter.service

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.NewsletterInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NewsletterService(
    private val newsletterRepository: NewsletterRepository,
    private val publicationService: PublicationService,
) {
    /**
     * Retrieves information on all enabled newsletters that are not hidden.
     *
     * @return a list of `NewsletterInfo` objects containing details about each non-hidden enabled newsletter.
     */
    fun getNonHiddenEnabledNewslettersInfo(): List<NewsletterInfo> {
        return newsletterRepository
            .getNonHiddenEnabledNewsletters()
            .map { createNewsletterInfo(it) }
    }

    private fun createNewsletterInfo(newsletter: Newsletter): NewsletterInfo {
        val stats = publicationService.getNewsletterStats(newsletter)
        return NewsletterInfo(
            code = newsletter.code,
            title = newsletter.name,
            websiteUrl = newsletter.websiteUrl,
            notes = newsletter.notes,
            stats = stats,
        )
    }

    /**
     * Finds a newsletter by its unique code.
     *
     * @param code The unique code of the newsletter.
     * @return The newsletter associated with the given code, or null if not found.
     */
    fun findNewsletterByCode(code: String): Newsletter? {
        return newsletterRepository.findNewsletterByCode(code)
    }

    /**
     * Finds an appropriate `NewsletterHandler` instance capable of handling the provided email.
     * If a newsletter is disabled, its handler won't be considered here.
     *
     * @param email The email to be checked against available newsletter handlers.
     * @return A `NewsletterHandler` capable of handling the email,
     * or null if none or multiple handlers are found.
     */
    fun findNewsletterHandlerForEmail(email: Email): NewsletterHandler? {
        return try {
            newsletterRepository.getEnabledNewsletterHandlers()
                .single { it.canHandle(email) }
        } catch (_: NoSuchElementException) {
            LOG.warn("No enabled handler found for email {}", email.subject)
            null
        } catch (_: IllegalArgumentException) {
            LOG.error("Too many handlers found for email {}", email.subject)
            null
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsletterService::class.java)
    }
}
