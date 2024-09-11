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
package fr.nicopico.n2rss.analytics.simpleanalytics

import fr.nicopico.n2rss.analytics.AnalyticsEvent
import fr.nicopico.n2rss.analytics.AnalyticsException
import fr.nicopico.n2rss.analytics.AnalyticsService
import fr.nicopico.n2rss.config.N2RssProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@Service
class SimpleAnalyticsService(
    restClientBuilder: RestClient.Builder = RestClient.builder(),
    private val analyticsApiBaseUrl: String,
    private val analyticsProperties: N2RssProperties.AnalyticsProperties,
) : AnalyticsService {

    private val simpleAnalyticsProperties = analyticsProperties.simpleAnalytics

    @Autowired
    constructor(
        restClientBuilder: RestClient.Builder,
        analyticsProperties: N2RssProperties.AnalyticsProperties,
    ) : this(
        restClientBuilder = restClientBuilder,
        analyticsApiBaseUrl = "https://queue.simpleanalyticscdn.com",
        analyticsProperties = analyticsProperties,
    )

    private val restClient by lazy {
        restClientBuilder
            .baseUrl(analyticsApiBaseUrl)
            .build()
    }

    @Throws(AnalyticsException::class)
    override fun track(event: AnalyticsEvent) {
        if (analyticsProperties.enabled && simpleAnalyticsProperties != null) {
            LOG.info("TRACK: $event")
            try {
                restClient
                    .post()
                    .uri("/events")
                    .header(
                        "User-Agent",
                        simpleAnalyticsProperties.userAgent
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event.toSimpleAnalyticsEvent(simpleAnalyticsProperties))
                    .retrieve()
                    .toBodilessEntity()
            } catch (e: HttpClientErrorException) {
                throw AnalyticsException("Unable to send analytics event $event", e)
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SimpleAnalyticsService::class.java)
    }
}
