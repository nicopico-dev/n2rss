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

import fr.nicopico.n2rss.config.CacheConfiguration
import fr.nicopico.n2rss.config.PersistenceMode
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.data.entity.ArticleEntity
import fr.nicopico.n2rss.newsletter.data.entity.PublicationEntity
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.Publication
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import fr.nicopico.n2rss.utils.toLegacyDate
import jakarta.transaction.Transactional
import kotlinx.datetime.LocalDate
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.net.URL

@Service
class PublicationService(
    private val publicationRepository: PublicationRepository,
    private val newsletterRepository: NewsletterRepository,
    private val persistenceMode: PersistenceMode,
) {
    //region getPublications
    @Transactional
    fun getPublications(newsletter: Newsletter, pageable: PageRequest): Page<Publication> {
        return when (persistenceMode) {
            PersistenceMode.DEFAULT -> getPublicationsFromMariaDB(newsletter, pageable)
        }
    }

    private fun getPublicationsFromMariaDB(
        newsletter: Newsletter,
        pageable: PageRequest
    ) = publicationRepository.findByNewsletterCode(newsletter.code, pageable)
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
    //endregion

    //region savePublications
    @Transactional
    @CacheEvict(
        // Clear feed cache when publications are saved
        cacheNames = [CacheConfiguration.GET_RSS_FEED_CACHE_NAME],
        allEntries = true,
    )
    fun savePublications(publications: List<Publication>) {
        val nonEmptyPublications = publications
            .filter { it.articles.isNotEmpty() }

        when (persistenceMode) {
            PersistenceMode.DEFAULT -> savePublicationsToMariaDB(nonEmptyPublications)
        }
    }

    private fun savePublicationsToMariaDB(publications: List<Publication>) {
        val entities = publications.map {
            PublicationEntity(
                title = it.title,
                date = it.date.toLegacyDate(),
                newsletterCode = it.newsletter.code,
            ).also { entity ->
                entity.articles = it.articles.map { article ->
                    ArticleEntity(
                        title = article.title,
                        link = article.link.toString(),
                        resolvedLink = null,
                        description = article.description,
                        publication = entity,
                    )
                }
            }
        }

        publicationRepository.saveAll(entities)
    }
    //endregion

    fun getPublicationsCount(newsletter: Newsletter): Long {
        val getDefaultCount: () -> Long = { publicationRepository.countPublicationsByNewsletterCode(newsletter.code) }

        return when (persistenceMode) {
            PersistenceMode.DEFAULT -> getDefaultCount()
        }
    }

    fun getOldestPublicationDate(newsletter: Newsletter): LocalDate? {
        val getDefaultOldest: () -> LocalDate? = {
            publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(newsletter.code)?.date?.toKotlinLocaleDate()
        }

        return when (persistenceMode) {
            PersistenceMode.DEFAULT -> getDefaultOldest()
        }
    }
}
