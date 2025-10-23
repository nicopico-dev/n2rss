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
import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import fr.nicopico.n2rss.newsletter.data.entity.ArticleEntity
import fr.nicopico.n2rss.newsletter.data.entity.PublicationEntity
import fr.nicopico.n2rss.newsletter.models.Article
import fr.nicopico.n2rss.newsletter.models.Newsletter
import fr.nicopico.n2rss.newsletter.models.NewsletterStats
import fr.nicopico.n2rss.newsletter.models.Publication
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import fr.nicopico.n2rss.utils.toLegacyDate
import jakarta.transaction.Transactional
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.net.URL
import kotlin.math.roundToInt

@Service
class PublicationService(
    private val publicationRepository: PublicationRepository,
    private val newsletterRepository: NewsletterRepository,
) {
    //region getPublications
    @Transactional
    fun getPublications(newsletter: Newsletter, pageable: PageRequest): Page<Publication> {
        return publicationRepository.findByNewsletterCode(newsletter.code, pageable)
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

        val entities = nonEmptyPublications.map {
            PublicationEntity(
                title = it.title,
                date = it.date.toLegacyDate(),
                newsletterCode = it.newsletter.code,
            ).also { entity ->
                entity.articles = it.articles.map { article ->
                    ArticleEntity(
                        title = article.title,
                        link = article.link.toString(),
                        description = article.description,
                        publication = entity,
                    )
                }
            }
        }
        publicationRepository.saveAll(entities)
    }
    //endregion

    @Transactional
    fun getNewsletterStats(newsletter: Newsletter): NewsletterStats {
        return when (
            val publicationCount = publicationRepository.countPublicationsByNewsletterCode(newsletter.code)
        ) {
            0L -> NewsletterStats.NoPublication
            1L -> NewsletterStats.SinglePublication(
                startingDate = newsletter.getStartingDate()
            )

            else -> {
                val latestPublications = publicationRepository.findByNewsletterCode(
                    newsletterCode = newsletter.code,
                    pageable = Pageable.ofSize(LATEST_PUBLICATIONS_COUNT),
                ).content


                NewsletterStats.MultiplePublications(
                    startingDate = newsletter.getStartingDate(),
                    publicationCount = publicationCount,
                    publicationPeriodicity = latestPublications.computeAveragePublicationPeriod(),
                    articlesPerPublication = latestPublications.computeAverageArticlesPerPublication(),
                )
            }
        }
    }

    private fun Newsletter.getStartingDate(): LocalDate {
        val firstPublication = requireNotNull(
            publicationRepository.findFirstByNewsletterCodeOrderByDateAsc(code)
        ) { "Newsletter must have at least one publication" }
        return firstPublication.date.toKotlinLocaleDate()
    }

    private fun List<PublicationEntity>.computeAveragePublicationPeriod(): DatePeriod {
        require(size >= 2) {
            "Computing average publication period requires at least 2 publications"
        }

        val publicationDates: List<LocalDate> = map { it.date.toKotlinLocaleDate() }.sorted()
        val averageDays: Int = publicationDates
            .zipWithNext { prev, next -> next - prev }
            .map { it.days }
            .average()
            .roundToInt()
        return DatePeriod(days = averageDays)
    }

    private fun List<PublicationEntity>.computeAverageArticlesPerPublication(): Int {
        require(size >= 2) {
            "Computing average articles per publication requires at least 2 publications"
        }

        return map { it.articles.size }.average().roundToInt()
    }

    companion object {
        private const val LATEST_PUBLICATIONS_COUNT = 10
    }
}
