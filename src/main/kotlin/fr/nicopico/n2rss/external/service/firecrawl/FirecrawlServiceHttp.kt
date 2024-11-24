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
package fr.nicopico.n2rss.external.service.firecrawl

import fr.nicopico.n2rss.external.service.firecrawl.dto.FirecrawlScrapeRequestDTO
import fr.nicopico.n2rss.external.service.firecrawl.dto.FirecrawlScrapeResponseDTO
import fr.nicopico.n2rss.utils.url.Url
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.util.concurrent.CompletableFuture


class FirecrawlServiceHttp(
    clientBuilder: RestClient.Builder = RestClient.builder(),
    baseUrl: String = "https://api.firecrawl.dev",
    accessToken: String,
) : FirecrawlService {

    private val restClient by lazy {
        clientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer $accessToken")
            .build()
    }

    /**
     * Scrape [url] using Firecrawl API
     * https://docs.firecrawl.dev/api-reference/endpoint/scrape
     */
    override fun scrape(url: Url): CompletableFuture<String> {
        val payload = FirecrawlScrapeRequestDTO(url.toString())
        val response: FirecrawlScrapeResponseDTO = try {
            restClient
                .post()
                .uri("/scrape")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body()
                ?: throw FirecrawlException("Cannot crawl URL, response body is empty")
        } catch (e: RestClientResponseException) {
            throw FirecrawlException("Failed to crawl URL", e)
        }

        LOG.debug("Scrapped {} ->\n{}", url.toString(), response)

        return if (response.success && response.data != null) {
            CompletableFuture
                .completedFuture(response.data.markdown)
        } else {
            CompletableFuture
                .failedFuture(FirecrawlException("Failed to crawl URL: ${response.error}"))
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FirecrawlServiceHttp::class.java)
    }
}
