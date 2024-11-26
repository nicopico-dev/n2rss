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

import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointRepository
import org.springframework.stereotype.Service
import java.net.URL
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
class FirecrawlMockService(
    private val temporaryEndpointRepository: TemporaryEndpointRepository,
) : FirecrawlService {

    @Suppress("TooGenericExceptionCaught")
    override fun scrape(url: URL): CompletableFuture<String> {
        return try {
            val tempId = url.path
                .split("/")
                .lastOrNull()
                .let { UUID.fromString(it) }

            val tempEndpoint = requireNotNull(
                temporaryEndpointRepository.findByExposedId(tempId)
            )
            val content = getContent(tempEndpoint.label)
            CompletableFuture.completedFuture(content)
        } catch (e: Exception) {
            CompletableFuture.failedFuture(e)
        }
    }

    private fun getContent(label: String): String {
        val filePath = when {
            label.startsWith("Tech Readers #116") -> "stubs/markdown/tech-readers #116.md"
            else -> throw NoSuchElementException("No content for [$label]")
        }

        return Paths.get(filePath)
            .toFile()
            .readText(Charsets.UTF_8)
    }
}
