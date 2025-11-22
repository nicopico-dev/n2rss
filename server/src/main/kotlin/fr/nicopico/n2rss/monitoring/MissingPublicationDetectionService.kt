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
package fr.nicopico.n2rss.monitoring

import fr.nicopico.n2rss.newsletter.data.NewsletterRepository
import fr.nicopico.n2rss.newsletter.handlers.newsletters
import fr.nicopico.n2rss.newsletter.models.NewsletterStats
import fr.nicopico.n2rss.newsletter.service.PublicationService
import fr.nicopico.n2rss.utils.now
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

@Service
class MissingPublicationDetectionService(
    private val newsletterRepository: NewsletterRepository,
    private val publicationService: PublicationService,
    private val monitoringService: MonitoringService,
    private val clock: Clock = Clock.System,
    private val tolerance: DatePeriod = DatePeriod(days = 2),
) {
    @Transactional
    @Scheduled(fixedDelay = 7, timeUnit = TimeUnit.DAYS)
    fun detectMissingPublications() {
        val threshold = LocalDate.now(clock) - tolerance

        val missingPublications = newsletterRepository
            .getEnabledNewsletterHandlers()
            .map { it.newsletters.first() }
            .filter { newsletter ->
                val stats = publicationService.getNewsletterStats(newsletter)
                stats is NewsletterStats.MultiplePublications
                    // Get all newsletters whose last publication should already have happened
                    && stats.lastPublicationDate + stats.publicationPeriodicity < threshold
            }

        missingPublications.forEach { newsletter ->
            monitoringService.notifyMissingPublication(newsletter)
        }
    }
}
