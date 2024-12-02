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

import fr.nicopico.n2rss.external.service.firecrawl.FirecrawlService
import fr.nicopico.n2rss.external.temporary.TemporaryEndpointService
import fr.nicopico.n2rss.external.temporary.TemporaryEndpointService.TemporaryEndpoint
import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.Sender
import fr.nicopico.n2rss.utils.now
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL
import java.util.concurrent.CompletableFuture

@ExtendWith(MockKExtension::class)
class FirecrawlEmailParserTest {

    @MockK
    private lateinit var temporaryEndpointService: TemporaryEndpointService
    @MockK
    private lateinit var firecrawlService: FirecrawlService

    private lateinit var parser: FirecrawlEmailParser

    @BeforeEach
    fun setUp() {
        parser = FirecrawlEmailParser(
            temporaryEndpointService = temporaryEndpointService,
            firecrawlService = firecrawlService,
        )
    }

    @Test
    fun `should parse email content`() {
        // GIVEN
        val email = Email(
            sender = Sender("sharmel_ensleyp8fe@accident.bo"),
            date = LocalDate.now(),
            subject = "origins",
            content = "Appointed bag implementing peace revolution written springer",
            msgnum = 1,
        )

        val temporaryUrl = URL("https://tutorial32yvssrd.ny")
        val temporaryEndpoint: TemporaryEndpoint = mockk {
            every { url } returns temporaryUrl
            every { close() } just Runs
        }
        val scrapeResult = "Beverages rising promised valium nationally biological residence"
        every { temporaryEndpointService.expose(any(), any()) } returns temporaryEndpoint
        every { firecrawlService.scrape(any()) } returns CompletableFuture.completedFuture(scrapeResult)

        // WHEN
        val result = parser.parseEmail(email)

        // THEN
        result shouldBe scrapeResult
        verifyOrder {
            temporaryEndpointService.expose(email.subject, email.content)
            firecrawlService.scrape(temporaryUrl)
            temporaryEndpoint.close()
        }
    }

    @Test
    fun `should handle parsing error`() {
        // GIVEN
        val email = Email(
            sender = Sender("delonte_jenkss@practice.jm"),
            date = LocalDate.now(),
            subject = "weekends",
            content = "Awarded chaos funky geek mess telecharger floor, different polished assured sapphire cities.",
            msgnum = 1,
        )

        val temporaryUrl = URL("https://footwearxrhv4b8yyfv.an")
        val temporaryEndpoint: TemporaryEndpoint = mockk {
            every { url } returns temporaryUrl
            every { close() } just Runs
        }
        val scrapeError = RuntimeException("TEST")
        every { temporaryEndpointService.expose(any(), any()) } returns temporaryEndpoint
        every { firecrawlService.scrape(any()) } returns CompletableFuture.failedFuture(scrapeError)

        // WHEN
        val error = shouldThrowAny {
            parser.parseEmail(email)
        }

        // THEN
        error.cause shouldBe scrapeError
        verifyOrder {
            temporaryEndpointService.expose(email.subject, email.content)
            firecrawlService.scrape(temporaryUrl)
            temporaryEndpoint.close()
        }
    }
}
