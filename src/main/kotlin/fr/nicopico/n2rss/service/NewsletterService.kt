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

package fr.nicopico.n2rss.service

import fr.nicopico.n2rss.data.NewsletterRequestRepository
import fr.nicopico.n2rss.data.PublicationRepository
import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.models.NewsletterInfo
import fr.nicopico.n2rss.models.NewsletterRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Service
class NewsletterService(
    private val newsletterHandlers: List<NewsletterHandler>,
    private val publicationRepository: PublicationRepository,
    private val newsletterRequestRepository: NewsletterRequestRepository,
) {
    fun getNewslettersInfo(): List<NewsletterInfo> {
        return newsletterHandlers
            .map { it.newsletter }
            .map {
                NewsletterInfo(
                    code = it.code,
                    title = it.name,
                    websiteUrl = it.websiteUrl,
                    publicationCount = publicationRepository.countPublicationsByNewsletter(it),
                    startingDate = publicationRepository.findFirstByNewsletterOrderByDateAsc(it)?.date,
                )
            }
    }

    @Transactional
    fun saveRequest(newsletterUrl: URL) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        // TODO Sanitize URL to prevent duplicates
        val request = newsletterRequestRepository.getByNewsletterUrl(newsletterUrl)

        val updatedRequest = request?.copy(
            lastRequestDate = now,
            requestCount = request.requestCount + 1
        ) ?: NewsletterRequest(
            newsletterUrl = newsletterUrl,
            firstRequestDate = now,
            lastRequestDate = now,
            requestCount = 1,
        )

        newsletterRequestRepository.save(updatedRequest)
    }
}
