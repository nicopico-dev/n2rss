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
package fr.nicopico.n2rss.external

import fr.nicopico.n2rss.external.service.firecrawl.FirecrawlService
import fr.nicopico.n2rss.external.temporary.TemporaryEndpointService
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.html
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class FirecrawlEmailParser(
    private val temporaryEndpointService: TemporaryEndpointService,
    private val firecrawlService: FirecrawlService,
) {
    @Async
    fun parseEmail(email: Email): String {
        val cleanedHtml = Jsoup.clean(
            email.content.html,
            Safelist.basic(),
        )
        return temporaryEndpointService.expose(email.subject, cleanedHtml)
            .use { endpoint ->
                firecrawlService.scrape(endpoint.url)
                    .get(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            }
    }

    companion object {
        private const val TIMEOUT_MS = 5_000L
    }
}
