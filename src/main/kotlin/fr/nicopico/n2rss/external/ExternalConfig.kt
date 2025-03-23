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
package fr.nicopico.n2rss.external

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.external.service.firecrawl.FirecrawlHttpService
import fr.nicopico.n2rss.external.service.firecrawl.FirecrawlMockService
import fr.nicopico.n2rss.external.service.firecrawl.FirecrawlService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class ExternalConfig {

    @Bean
    @Qualifier(BASE_URL_QUALIFIER)
    fun baseUrl(externalProperties: N2RssProperties.ExternalProperties): URL {
        return externalProperties.baseUrl
    }

    @Bean
    fun firecrawlService(
        externalProperties: N2RssProperties.ExternalProperties,
        mockService: FirecrawlMockService,
        httpService: FirecrawlHttpService,
    ): FirecrawlService {
        return if (externalProperties.mockExternalApis) {
            mockService
        } else {
            httpService
        }
    }

    @Bean
    fun firecrawlHttpService(
        externalProperties: N2RssProperties.ExternalProperties,
    ): FirecrawlHttpService {
        return FirecrawlHttpService(
            accessToken = externalProperties.firecrawlToken
        )
    }

    companion object {
        const val BASE_URL_QUALIFIER = "base-url"
    }
}
