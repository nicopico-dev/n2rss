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
package fr.nicopico.n2rss.config

import fr.nicopico.n2rss.mail.newsletter.NewsletterHandler
import fr.nicopico.n2rss.service.ReCaptchaService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val LOG = LoggerFactory.getLogger(NewsletterConfiguration::class.java)

@Configuration
class NewsletterConfiguration(
    private val applicationContext: ApplicationContext,
    private val feedsProperties: N2RssProperties.FeedsProperties,
) {
    @Bean(ENABLED_NEWSLETTER_HANDLERS)
    fun newsletterHandlers(): List<NewsletterHandler> {
        val disabledNewsletters = feedsProperties.disabledNewsletters

        LOG.warn("Disabled newsletters: {}", disabledNewsletters)

        return applicationContext
            .getBeansOfType(NewsletterHandler::class.java)
            .values
            .filterNot {
                it.newsletter.code in disabledNewsletters
            }
            .toList()
    }

    companion object {
        const val ENABLED_NEWSLETTER_HANDLERS = "enabled_newsletter_handlers"
    }
}
