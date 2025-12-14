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

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl
import fr.nicopico.n2rss.config.CacheConfiguration
import fr.nicopico.n2rss.utils.toLegacyDate
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class RssService(
    private val newsletterService: NewsletterService,
    private val publicationService: PublicationService,
) {

    /**
     * Retrieves an RSS feed for the given newsletter code.
     *
     * @param code The code of the newsletter.
     * @param publicationStart The start index of the publications to include in the feed.
     * @param publicationCount The number of publications to include in the feed.
     * @return The RSS feed as a [SyndFeed] object.
     * @throws NoSuchElementException If the newsletter with the given code is not found.
     */
    @Cacheable(cacheNames = [CacheConfiguration.GET_RSS_FEED_CACHE_NAME])
    fun getFeed(code: String, publicationStart: Int, publicationCount: Int): SyndFeed {
        val newsletter = newsletterService.findNewsletterByCode(code)
            ?: throw NoSuchElementException("Newsletter with code $code was not found")

        val feed = SyndFeedImpl().apply {
            feedType = "rss_2.0"
            title = newsletter.feedTitle
            link = newsletter.websiteUrl
            description = "This is an RSS Feed for the newsletter \"${newsletter.feedTitle}\""
        }

        val sort = Sort.by(Sort.Direction.DESC, "date")
        val pageable = PageRequest.of(publicationStart, publicationCount, sort)
        val publicationPage = publicationService.getPublications(newsletter, pageable)

        feed.entries = publicationPage.content
            .flatMap { publication ->
                publication.articles
                    .map { article ->
                        SyndEntryImpl().apply {
                            title = article.title
                            link = article.link.toString()
                            description = SyndContentImpl().apply {
                                type = "text/html"
                                value = article.description.restoreHtmlLineFeeds()
                            }
                            publishedDate = publication.date.toLegacyDate()
                        }
                    }
            }

        return feed
    }

    private fun String.restoreHtmlLineFeeds(): String {
        val isLikelyHtml = contains('<') && contains('>')
        return if (isLikelyHtml) {
            this // assume already HTML
        } else {
            val normalized = replace("\r\n", "\n")
            val paragraphs = normalized.split("\n\n+").flatMap { it.split("\n\n") }
            paragraphs.joinToString(separator = "</p><p>") { p ->
                p.split('\n').joinToString("<br/>")
            }.let { "<p>$it</p>" }
        }
    }
}
