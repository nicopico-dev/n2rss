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
package fr.nicopico.n2rss.utils.url

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI

class RestClientExtKtTest {

    private val server = MockWebServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should resolve an URI with 302 redirect`() {
        // GIVEN
        val userAgent = "UA"
        val resolvedUrl = server.url("/redirect/url")
        server.enqueue(
            // Redirect
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", resolvedUrl)
        )
        server.enqueue(
            // Success
            MockResponse()
                .setResponseCode(200)
        )

        val originalUri: URI = server.url("/original/url").toUri()

        // WHEN
        val result = runBlocking {
            resolveUris(userAgent, listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to resolvedUrl.toUri())
    }

    @Test
    fun `should follow multiple redirects to resolve an URI`() {
        // GIVEN
        val userAgent = "UA"
        val originalUri: URI = server.url("/original/url").toUri()
        val resolvedUri: URI = server.url("/redirect2").toUri()

        // follow "beehiiv" redirection behavior
        // 302 Found
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("location", server.url("/redirect1"))
        )
        // 301 Moved permanently (with a relative path)
        val resolvedRelativePath = resolvedUri
        server.enqueue(
            MockResponse()
                .setResponseCode(301)
                .addHeader("location", resolvedRelativePath)
        )
        // 304 - Not Modified
        server.enqueue(
            MockResponse()
                .setResponseCode(304)
        )

        // WHEN
        val result = runBlocking {
            resolveUris(userAgent, listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to resolvedUri)
    }

    @Test
    fun `should map to null if the original URI if not accessible`() {
        // GIVEN
        val userAgent = "UA"
        server.enqueue(
            // Error 403 Forbidden
            MockResponse().setResponseCode(403)
        )

        val originalUri: URI = server.url("/original/url").toUri()

        // WHEN
        val result = runBlocking {
            resolveUris(userAgent, listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to null)
    }
}
