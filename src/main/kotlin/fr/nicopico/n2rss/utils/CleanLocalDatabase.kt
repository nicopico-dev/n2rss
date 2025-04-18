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
package fr.nicopico.n2rss.utils

import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointRepository
import fr.nicopico.n2rss.newsletter.data.PublicationRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Profile("local & reset-db")
@Component
class CleanLocalDatabase(
    private val publicationRepository: PublicationRepository,
    private val temporaryEndpointRepository: TemporaryEndpointRepository,
) {
    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun onApplicationEvent(ignored: ContextRefreshedEvent) {
        LOG.info("Clean-up local database...")
        publicationRepository.deleteAll()
        temporaryEndpointRepository.deleteAll()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CleanLocalDatabase::class.java)
    }
}
