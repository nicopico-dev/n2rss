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

import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.beEmpty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class FirecrawlServiceHttpIntegrationTest {

    private lateinit var firecrawlService: FirecrawlService

    @BeforeEach
    fun setUp() {
        val accessToken: String? = System.getenv("FIRECRAWL_TOKEN")

        // Only enable Firecrawl when the token environment variable is available
        firecrawlService = if (accessToken != null) {
            FirecrawlHttpService(accessToken = accessToken)
        } else {
            println("Firecrawl service disable")
            object : FirecrawlService {
                override fun scrape(url: URL): CompletableFuture<String> {
                    return CompletableFuture.completedFuture("MOCKED DATA")
                }
            }
        }
    }

    @Test
    fun `scrape Tech Readers #116 (Remote)`() {
        val response = firecrawlService.scrape(URL("https://www.nicopico.fr/firecrawl_test.html"))
            .get(5, TimeUnit.SECONDS)

        println(response)

        response shouldNot beNull()
        response shouldNot beEmpty()
    }
}
