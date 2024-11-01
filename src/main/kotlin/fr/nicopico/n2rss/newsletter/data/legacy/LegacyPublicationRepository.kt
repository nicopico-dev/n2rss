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
package fr.nicopico.n2rss.newsletter.data.legacy

import fr.nicopico.n2rss.config.CacheConfiguration
import fr.nicopico.n2rss.newsletter.models.Newsletter
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface LegacyPublicationRepository : MongoRepository<PublicationDocument, UUID> {
    fun findByNewsletter(newsletter: Newsletter, pageable: Pageable): Page<PublicationDocument>
    fun countPublicationsByNewsletter(newsletter: Newsletter): Long
    fun findFirstByNewsletterOrderByDateAsc(newsletter: Newsletter): PublicationDocument?

    // Clear feed cache when publications are saved
    @CacheEvict(
        cacheNames = [CacheConfiguration.GET_RSS_FEED_CACHE_NAME],
        allEntries = true,
    )
    override fun <S : PublicationDocument> saveAll(entities: Iterable<S>): List<S>
}
