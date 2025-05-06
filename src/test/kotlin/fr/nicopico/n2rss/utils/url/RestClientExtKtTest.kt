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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds

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
            resolveUris(listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to resolvedUrl.toUri())
    }

    @Test
    fun `should follow multiple redirects to resolve an URI`() {
        // GIVEN
        val originalUri: URI = server.url("/original/url").toUri()
        val resolvedUri: URI = server.url("/redirect2/url").toUri()

        // follow "beehiiv" redirection behavior
        // 302 Found
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("location", server.url("/redirect1"))
        )
        // 301 Moved permanently (with a relative path)
        server.enqueue(
            MockResponse()
                .setResponseCode(301)
                .addHeader("location", resolvedUri)
        )
        // 304 - Not Modified
        server.enqueue(
            MockResponse()
                .setResponseCode(304)
        )

        // WHEN
        val result = runBlocking {
            resolveUris(listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to resolvedUri)
    }

    @Test
    fun `should not crash on relative redirects`() {
        // GIVEN
        val originalUri: URI = server.url("/original/url").toUri()
        val relativeRedirectPath = "/redirect/url"

        server.enqueue(
            MockResponse()
                .setResponseCode(301)
                .addHeader("location", relativeRedirectPath)
        )

        // WHEN
        val result = runBlocking {
            resolveUris(listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to null)
    }

    @Test
    fun `should map to null if the original URI if not accessible`() {
        // GIVEN
        server.enqueue(
            // Error 403 Forbidden
            MockResponse().setResponseCode(403)
        )

        val originalUri: URI = server.url("/original/url").toUri()

        // WHEN
        val result = runBlocking {
            resolveUris(listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to null)
    }

    @Test
    fun `should map unsupported 'beehiiv' to null without a request`() {
        // GIVEN
        val originalUri = URI("https://link.mail.beehiiv.com/something")

        // WHEN
        val result = runBlocking {
            resolveUris(listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to null)
    }

    @Test
    fun `should map to null if the redirect location is invalid`() {
        // GIVEN
        server.enqueue(
            MockResponse()
                .setResponseCode(301)
                .addHeader("location", "this is not a URI")
        )

        val originalUri: URI = server.url("/original/url").toUri()

        // WHEN
        val result = runBlocking {
            resolveUris(listOf(originalUri))
        }

        // THEN
        result shouldBe mapOf(originalUri to null)
    }

    @Test
    fun `should map to null if the server does not respond`() {
        // GIVEN
        val originalUri: URI = server.url("/original/url").toUri()

        // WHEN
        val result = runBlocking {
            resolveUris(
                urls = listOf(originalUri),
                timeout = 20.milliseconds
            )
        }

        // THEN
        result shouldBe mapOf(originalUri to null)
    }

    @Disabled
    @Test
    fun `should resolve 'beehiiv' urls`() {
        val beehiivUrl =
            URI("https://link.mail.beehiiv.com/ss/c/u001.3a5P_SwQzY5x8USD2q4p0r7tZ4Xc_IMfzOhNH-sZPqXF5edqv_aYXhBXdCzcRVykDyq9Wl9a1Ge1hLkoeCwntpQkvfL-5h3Xz0oMO0MNUb4JOZkpL15kAwrX55aT-RUjLDbPVBr78sxN0T17LYZS4Ar3QuxRWfDySw2dWCyJIvNwmllgb9FqevhlQtIWQwJomy11i66WPmIY76Tj39FzlimpG3Ylm2Ay4QNS_wmQzYhLJrM85OjSu_-5vq3RR7SolPqm7MhUq_R5Y3pn_KLdo_cS7p1n9gckSSXS1rLk9LH9PWAEdKGOPayosqQ2YCNR/4bo/zbVYybWiRwSiQr1bey4J-w/h10/h001.2nd4OWD1oRoMgzUpVtSORAo4DI3VH1mAH-36d0sUrRg")
        val expectedUrl = URI("https://www.qawolf.com/webinars/ai-prompt-evaluations-beyond-golden-datasets")

        val result = runBlocking {
            resolveUris(listOf(beehiivUrl))
        }

        result[beehiivUrl] shouldBe expectedUrl
    }
}
