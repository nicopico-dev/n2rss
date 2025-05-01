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

import fr.nicopico.n2rss.newsletter.data.ArticleRepository
import fr.nicopico.n2rss.utils.url.resolveUris
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

private const val ARTICLE_RESOLVE_URI_BATCH_SIZE = 15

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
) {
    // Disabled temporarily
    //@Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    @Suppress("unused")
    @Transactional
    fun saveResolvedUrls() {
        val pageable = PageRequest.ofSize(ARTICLE_RESOLVE_URI_BATCH_SIZE)
        val articles = articleRepository.findByResolvedLinkIsNull(pageable)
            .content
            .toMutableList()
        LOG.debug("Resolving urls for ${articles.size} articles...")

        val resolvedUris = runBlocking {
            resolveUris(articles.map { URI(it.link) })
        }

        articles.replaceAll { article ->
            article.copy(
                resolvedLink = resolvedUris[URI(article.link)]?.toString() ?: article.link,
            )
        }
        articleRepository.saveAll(articles)
        LOG.debug("Saved resolved url for articles {}", articles.map { it.title })
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ArticleService::class.java)
    }
}
