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
package fr.nicopico.n2rss.analytics

import fr.nicopico.n2rss.analytics.data.AnalyticsRepository
import fr.nicopico.n2rss.analytics.data.toAnalyticsData
import fr.nicopico.n2rss.config.N2RssProperties
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

private val LOG = LoggerFactory.getLogger(AnalyticService::class.java)

@Service
class AnalyticService(
    private val analyticsRepository: AnalyticsRepository,
    private val analyticsProperties: N2RssProperties.AnalyticsProperties,
    private val clock: Clock,
) {
    @Autowired
    constructor(
        analyticsRepository: AnalyticsRepository,
        analyticsProperties: N2RssProperties.AnalyticsProperties,
    ) : this(analyticsRepository, analyticsProperties, Clock.System)

    @Throws(AnalyticException::class)
    fun track(event: AnalyticEvent) {
        if (analyticsProperties.enabled) {
            LOG.info("TRACK: $event")
            try {
                val data = event.toAnalyticsData(clock.now())
                analyticsRepository.save(data)
            } catch (e: Exception) {
                throw AnalyticException("Unable to send analytics event $event", e)
            }
        }
    }
}
