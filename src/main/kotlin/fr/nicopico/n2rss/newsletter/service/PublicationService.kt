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

import fr.nicopico.n2rss.config.PersistenceMode
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.data.entity.ArticleEntity
import fr.nicopico.n2rss.newsletter.data.entity.PublicationEntity
import fr.nicopico.n2rss.newsletter.data.legacy.LegacyPublicationRepository
import fr.nicopico.n2rss.newsletter.data.legacy.PublicationDocument
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import fr.nicopico.n2rss.utils.toLegacyDate
import jakarta.transaction.Transactional
import kotlinx.datetime.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.net.URL

@Service
class PublicationService(
    private val publicationRepository: PublicationRepository,
    private val legacyPublicationRepository: LegacyPublicationRepository,
    private val newsletterRepository: NewsletterRepository,
    private val persistenceMode: PersistenceMode,
) {
    @Transactional
    fun getPublications(newsletter: Newsletter, pageable: PageRequest): Page<Publication> {
        return if (persistenceMode == PersistenceMode.LEGACY) {
            legacyPublicationRepository.findByNewsletter(newsletter, pageable)
                .map {
                    Publication(
                        title = it.title,
                        date = it.date,
                        newsletter = it.newsletter,
                        articles = it.articles,
                    )
                }
        } else {
            publicationRepository.findByNewsletterCode(newsletter.code, pageable)
                .map {
                    Publication(
                        title = it.title,
                        date = it.date.toKotlinLocaleDate(),
                        newsletter = newsletterRepository.findNewsletterByCode(it.newsletterCode)!!,
                        articles = it.articles.map { article ->
                            Article(
                                title = article.title,
                                link = URL(article.link),
                                description = article.description,
                            )
                        },
                    )
                }
        }
    }

    @Transactional
    fun savePublications(publications: List<Publication>) {
        val nonEmptyPublications = publications
            .filter { it.articles.isNotEmpty() }

        when (persistenceMode) {
            PersistenceMode.LEGACY -> saveToMongoDb(nonEmptyPublications)
            PersistenceMode.MIGRATION -> {
                saveToMongoDb(nonEmptyPublications)
                saveToMariaDB(nonEmptyPublications)
            }

            PersistenceMode.DEFAULT -> saveToMariaDB(nonEmptyPublications)
        }
    }

    private fun saveToMongoDb(publications: List<Publication>) {
        val publicationDocuments = publications.map {
            PublicationDocument(
                title = it.title,
                date = it.date,
                newsletter = it.newsletter,
                articles = it.articles,
            )
        }
        legacyPublicationRepository.saveAll(publicationDocuments)
    }

    private fun saveToMariaDB(publications: List<Publication>) {
        val entities = publications.map {
            PublicationEntity(
                title = it.title,
                date = it.date.toLegacyDate(),
                newsletterCode = it.newsletter.code,
                articles = it.articles.map { article ->
                    ArticleEntity(
                        title = article.title,
                        link = article.link.toString(),
                        description = article.description,
                    )
                },
            )
        }
        publicationRepository.saveAll(entities)
    }

    fun getPublicationsCount(newsletter: Newsletter): Long {
        return if (persistenceMode == PersistenceMode.LEGACY) {
            legacyPublicationRepository.countPublicationsByNewsletter(newsletter)
        } else {
            publicationRepository.countPublicationsByNewsletterCode(newsletter.code)
        }
    }

    fun getLatestPublicationDate(newsletter: Newsletter): LocalDate? {
        return if (persistenceMode == PersistenceMode.LEGACY) {
            legacyPublicationRepository.findFirstByNewsletterOrderByDateAsc(newsletter)?.date
        } else {
            publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletter.code)?.date?.toKotlinLocaleDate()
        }
    }
}
