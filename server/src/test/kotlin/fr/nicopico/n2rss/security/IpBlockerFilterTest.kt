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

import fr.nicopico.n2rss.analytics.models.AnalyticsEvent
import fr.nicopico.n2rss.analytics.service.AnalyticsService
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class IpBlockerFilterTest {

    @Test
    fun `should block IP if it matches one of the patterns`() {
        // GIVEN
        val blockedPatterns = listOf("192.168.1.*", "10.**")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "192.168.1.10"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        filterChain.request shouldBe null // filterChain.doFilter was not called
        verify {
            analyticsService.track(
                AnalyticsEvent.BlockedIpRequestEvent(
                    userAgent = "Test-Agent",
                    clientIpAddress = "192.168.1.10",
                    requestedUrl = "/test/path"
                )
            )
        }
    }

    @Test
    fun `should allow IP if it does not match any pattern`() {
        // GIVEN
        val blockedPatterns = listOf("192.168.1.*", "10.**")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "192.168.2.1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_OK
        filterChain.request shouldBe request
        verify(exactly = 0) { analyticsService.track(any()) }
    }

    @Test
    fun `should match IPv4 with star pattern`() {
        // GIVEN
        val blockedPatterns = listOf("192.168.*.1")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "192.168.10.1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should match IPv4 with double star pattern`() {
        // GIVEN
        val blockedPatterns = listOf("10.**")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "10.1.2.3"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should match IPv6 with patterns`() {
        // GIVEN
        val blockedPatterns = listOf("2001:db8:**")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "2001:db8:0:0:0:0:0:1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should match compressed IPv6`() {
        // GIVEN
        val blockedPatterns = listOf("2001:db8::*")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "2001:db8::1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should match compressed IPv6 with double star pattern`() {
        // GIVEN
        val blockedPatterns = listOf("2001:db8:**")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "2001:db8::1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should match compressed IP against expanded pattern`() {
        // GIVEN
        val blockedPatterns = listOf("2001:db8:0:0:0:0:0:1")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "2001:db8::1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should match IPv6 with star pattern between colons`() {
        // GIVEN
        val blockedPatterns = listOf("2001:*:0:0:0:0:0:1")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "2001:db8:0:0:0:0:0:1"
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify { analyticsService.track(any<AnalyticsEvent.BlockedIpRequestEvent>()) }
    }

    @Test
    fun `should ignore X-Forwarded-For header if no trusted proxies are configured`() {
        // GIVEN
        val blockedPatterns = listOf("203.0.113.195")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, emptyList(), analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "127.0.0.1"
            addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18")
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        // Allowed because 127.0.0.1 is not blocked, and X-Forwarded-For is ignored
        response.status shouldBe HttpServletResponse.SC_OK
        verify(exactly = 0) { analyticsService.track(any()) }
    }

    @Test
    fun `should use X-Forwarded-For header if it comes from a trusted proxy`() {
        // GIVEN
        val blockedPatterns = listOf("203.0.113.195")
        val trustedProxies = listOf("127.0.0.1")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, trustedProxies, analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "127.0.0.1"
            addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18")
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        verify {
            analyticsService.track(
                AnalyticsEvent.BlockedIpRequestEvent(
                    userAgent = "Test-Agent",
                    clientIpAddress = "203.0.113.195",
                    requestedUrl = "/test/path"
                )
            )
        }
    }

    @Test
    fun `should ignore X-Forwarded-For header if it comes from an untrusted proxy`() {
        // GIVEN
        val blockedPatterns = listOf("203.0.113.195")
        val trustedProxies = listOf("192.168.1.1")
        val analyticsService = mockk<AnalyticsService>(relaxed = true)
        val filter = IpBlockerFilter(blockedPatterns, trustedProxies, analyticsService)
        val request = MockHttpServletRequest().apply {
            remoteAddr = "127.0.0.1"
            addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18")
            addHeader("User-Agent", "Test-Agent")
            requestURI = "/test/path"
        }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // WHEN
        filter.doFilter(request, response, filterChain)

        // THEN
        // Allowed because 127.0.0.1 is not blocked, and X-Forwarded-For is ignored
        response.status shouldBe HttpServletResponse.SC_OK
        verify(exactly = 0) { analyticsService.track(any()) }
    }
}
