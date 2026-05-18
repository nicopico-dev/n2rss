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

package fr.nicopico.n2rss.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class RateLimitFilterTest {

    private lateinit var rateLimiterService: RateLimiterService
    private lateinit var filter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        rateLimiterService = mockk()
        filter = RateLimitFilter(rateLimiterService, emptyList(), true)
    }

    @Test
    fun `should allow request when rate limit is not exceeded`() {
        // GIVEN
        val clientIp = "203.0.113.1"
        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        val bucket = Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, 1.minutes.toJavaDuration())
                    .build()
            )
            .build()

        every { rateLimiterService.resolveBucket(clientIp) } returns bucket

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpStatus.OK.value()
        response.getHeader("X-Rate-Limit-Remaining") shouldBe "9"
        filterChain.request shouldBe request
        verify { rateLimiterService.resolveBucket(clientIp) }
    }

    @Test
    fun `should block request when rate limit is exceeded`() {
        // GIVEN
        val clientIp = "203.0.113.2"
        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // Bucket with 0 capacity to simulate exceeded limit
        val bucket = Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(1)
                    .refillIntervally(1, 1.minutes.toJavaDuration())
                    .build()
            )
            .build()
        bucket.tryConsume(1) // Consume the only token

        every { rateLimiterService.resolveBucket(clientIp) } returns bucket

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType shouldBe "text/plain"
        response.contentAsString shouldBe "Too many requests"
        response.getHeader("Retry-After") shouldNotBeNull {
            toInt() shouldBeGreaterThanOrEqual 60
        }
        verify { rateLimiterService.resolveBucket(clientIp) }
        filterChain.request shouldBe null
    }

    @Test
    fun `should use X-Forwarded-For header for client IP if present`() {
        // GIVEN
        val clientIp = "192.168.1.1"
        val forwardedIp = "203.0.113.195"
        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
            addHeader("X-Forwarded-For", "$forwardedIp, 70.41.3.18, 150.172.238.178")
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        val bucket = Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, 1.minutes.toJavaDuration())
                    .build()
            )
            .build()

        every { rateLimiterService.resolveBucket(forwardedIp) } returns bucket

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        verify { rateLimiterService.resolveBucket(forwardedIp) }
        verify(exactly = 0) {
            rateLimiterService.resolveBucket(clientIp)
        }
    }

    @Test
    fun `should use X-Forwarded-For header if it comes from a trusted proxy`() {
        // GIVEN
        val clientIp = "192.168.1.100"
        val forwardedIp = "203.0.113.195"
        val trustedProxies = listOf("192.168.1.100")
        val filterWithTrustedProxy = RateLimitFilter(rateLimiterService, trustedProxies, true)

        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
            addHeader("X-Forwarded-For", forwardedIp)
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        val bucket = Bucket.builder()
            .addLimit(Bandwidth.builder().capacity(10).refillIntervally(10, 1.minutes.toJavaDuration()).build())
            .build()

        every { rateLimiterService.resolveBucket(forwardedIp) } returns bucket

        // WHEN
        filterWithTrustedProxy.doFilter(request, response, filterChain)

        // THEN
        verify { rateLimiterService.resolveBucket(forwardedIp) }
    }

    @Test
    fun `should ignore X-Forwarded-For header if it comes from an untrusted proxy`() {
        // GIVEN
        val clientIp = "192.168.1.2"
        val forwardedIp = "203.0.113.195"
        val trustedProxies = listOf("192.168.1.1")
        val filterWithTrustedProxy = RateLimitFilter(rateLimiterService, trustedProxies, true)

        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
            addHeader("X-Forwarded-For", forwardedIp)
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        val bucket = Bucket.builder()
            .addLimit(Bandwidth.builder().capacity(10).refillIntervally(10, 1.minutes.toJavaDuration()).build())
            .build()

        every { rateLimiterService.resolveBucket(clientIp) } returns bucket

        // WHEN
        filterWithTrustedProxy.doFilter(request, response, filterChain)

        // THEN
        verify { rateLimiterService.resolveBucket(clientIp) }
        verify(exactly = 0) { rateLimiterService.resolveBucket(forwardedIp) }
    }

    @Test
    fun `should ignore loopback IPv4 requests`() {
        // GIVEN
        val clientIp = "127.0.0.1"
        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpStatus.OK.value()
        filterChain.request shouldBe request
        verify(exactly = 0) { rateLimiterService.resolveBucket(any()) }
    }

    @Test
    fun `should ignore loopback IPv6 requests`() {
        // GIVEN
        val clientIp = "::1"
        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpStatus.OK.value()
        filterChain.request shouldBe request
        verify(exactly = 0) { rateLimiterService.resolveBucket(any()) }
    }

    @Test
    fun `should bypass rate limiting when disabled`() {
        // GIVEN
        val clientIp = "203.0.113.50"
        val filterWithDisabledRateLimiting = RateLimitFilter(rateLimiterService, emptyList(), false)

        val request = MockHttpServletRequest().apply {
            remoteAddr = clientIp
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filterWithDisabledRateLimiting.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpStatus.OK.value()
        filterChain.request shouldBe request
        verify(exactly = 0) { rateLimiterService.resolveBucket(any()) }
    }
}
