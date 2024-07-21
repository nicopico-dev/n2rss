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
package fr.nicopico.n2rss.analytics.data

import fr.nicopico.n2rss.analytics.AnalyticsEvent
import fr.nicopico.n2rss.analytics.AnalyticsException
import fr.nicopico.n2rss.analytics.AnalyticsService
import fr.nicopico.n2rss.config.N2RssProperties
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DataAnalyticsService(
    private val analyticsRepository: AnalyticsRepository,
    private val analyticsProperties: N2RssProperties.AnalyticsProperties,
    private val clock: Clock,
) : AnalyticsService {
    @Autowired
    constructor(
        analyticsRepository: AnalyticsRepository,
        analyticsProperties: N2RssProperties.AnalyticsProperties,
    ) : this(analyticsRepository, analyticsProperties, Clock.System)

    @Throws(AnalyticsException::class)
    override fun track(event: AnalyticsEvent) {
        if (analyticsProperties.enabled) {
            LOG.info("TRACK: $event")
            // We want to catch all possible issues here
            @Suppress("TooGenericExceptionCaught")
            try {
                val data = event.toAnalyticsData(clock.now())
                analyticsRepository.save(data)
            } catch (e: Exception) {
                throw AnalyticsException("Unable to send analytics event $event", e)
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DataAnalyticsService::class.java)
    }
}
