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

import fr.nicopico.n2rss.analytics.service.AnalyticsService
import fr.nicopico.n2rss.analytics.service.AnalyticsServiceDelegate
import fr.nicopico.n2rss.analytics.service.NoOpAnalyticsService
import fr.nicopico.n2rss.analytics.service.simpleanalytics.SimpleAnalyticsService
import fr.nicopico.n2rss.config.N2RssProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AnalyticsConfig {

    @Bean
    fun analyticsService(
        analyticsProperties: N2RssProperties.AnalyticsProperties,
        simpleAnalyticsService: SimpleAnalyticsService,
    ): AnalyticsService {
        val profiles = analyticsProperties.analyticsProfiles
        val enabledServices: List<AnalyticsService> = profiles
            .distinct()
            .mapNotNull {
                when (it) {
                    "simple-analytics" -> simpleAnalyticsService
                    else -> null
                }
            }

        val analyticsService = if (enabledServices.isEmpty()) {
            NoOpAnalyticsService
        } else if (enabledServices.size == 1) {
            enabledServices[0]
        } else {
            AnalyticsServiceDelegate(enabledServices)
        }

        LOG.info("Analytics services enabled: {}", enabledServices)

        return analyticsService
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AnalyticsConfig::class.java)
    }
}
