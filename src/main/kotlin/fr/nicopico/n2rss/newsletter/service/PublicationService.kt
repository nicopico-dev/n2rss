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

package fr.nicopico.n2rss.newsletter.service

import fr.nicopico.n2rss.newsletter.data.legacy.LegacyPublicationRepository
import fr.nicopico.n2rss.newsletter.data.legacy.PublicationDocument
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import kotlinx.datetime.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class PublicationService(
    private val legacyPublicationRepository: LegacyPublicationRepository
) {
    fun getPublications(newsletter: Newsletter, pageable: PageRequest): Page<Publication> {
        return legacyPublicationRepository.findByNewsletter(newsletter, pageable)
            .map {
                Publication(
                    id = it.id,
                    title = it.title,
                    date = it.date,
                    newsletter = it.newsletter,
                    articles = it.articles,
                )
            }
    }

    fun savePublications(publications: List<Publication>) {
        val nonEmptyPublications = publications
            .filter { it.articles.isNotEmpty() }
            .map {
                PublicationDocument(
                    id = it.id,
                    title = it.title,
                    date = it.date,
                    newsletter = it.newsletter,
                    articles = it.articles,
                )
            }
        legacyPublicationRepository.saveAll(nonEmptyPublications)
    }

    fun getPublicationsCount(newsletter: Newsletter): Long {
        return legacyPublicationRepository.countPublicationsByNewsletter(newsletter)
    }

    fun getLatestPublicationDate(newsletter: Newsletter): LocalDate? {
        return legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter)?.date
    }
}
