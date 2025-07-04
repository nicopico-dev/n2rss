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
package fr.nicopico.n2rss.newsletter.data

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.newsletter.NewsletterConfiguration
import fr.nicopico.n2rss.newsletter.handlers.NewsletterHandler
import fr.nicopico.n2rss.newsletter.handlers.newsletters
import fr.nicopico.n2rss.newsletter.models.Newsletter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository

@Repository
class NewsletterRepository(
    private val allNewsletterHandlers: List<NewsletterHandler>,
    @param:Qualifier(NewsletterConfiguration.ENABLED_NEWSLETTER_HANDLERS)
    private val enabledNewsletterHandlers: List<NewsletterHandler>,
    private val feedsProperties: N2RssProperties.FeedsProperties,
) {
    fun findNewsletterByCode(code: String): Newsletter? {
        return allNewsletterHandlers
            .flatMap { it.newsletters }
            .firstOrNull { it.code == code }
    }

    fun getEnabledNewsletters(): List<Newsletter> {
        return enabledNewsletterHandlers.flatMap { it.newsletters }
    }

    fun getNonHiddenEnabledNewsletters(): List<Newsletter> {
        val hiddenNewsletters = feedsProperties.hiddenNewsletters
        return getEnabledNewsletters()
            .filter { it.code !in hiddenNewsletters }
    }

    fun getEnabledNewsletterHandlers(): List<NewsletterHandler> = enabledNewsletterHandlers
}
