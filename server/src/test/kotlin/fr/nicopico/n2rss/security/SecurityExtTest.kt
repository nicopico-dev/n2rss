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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class SecurityExtTest {

    @Nested
    inner class GetClientIpTest {
        @Test
        fun `should return remoteAddr if X-Forwarded-For is missing`() {
            val request = MockHttpServletRequest().apply {
                remoteAddr = "192.168.1.1"
            }
            request.getClientIp() shouldBe "192.168.1.1"
        }

        @Test
        fun `should return remoteAddr if X-Forwarded-For is present but no trusted proxies are configured`() {
            val request = MockHttpServletRequest().apply {
                remoteAddr = "192.168.1.1"
                addHeader("X-Forwarded-For", "203.0.113.195")
            }
            // Regression test: ignore X-Forwarded-For when no trusted proxies are configured.
            request.getClientIp(emptyList()) shouldBe "192.168.1.1"
        }

        @Test
        fun `should return first IP from X-Forwarded-For if remoteAddr is a trusted proxy`() {
            val request = MockHttpServletRequest().apply {
                remoteAddr = "127.0.0.1"
                addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18")
            }
            request.getClientIp(listOf("127.0.0.1")) shouldBe "203.0.113.195"
        }

        @Test
        fun `should return remoteAddr if X-Forwarded-For is present but remoteAddr is not trusted`() {
            val request = MockHttpServletRequest().apply {
                remoteAddr = "192.168.1.2"
                addHeader("X-Forwarded-For", "203.0.113.195")
            }
            request.getClientIp(listOf("192.168.1.1")) shouldBe "192.168.1.2"
        }
    }

    @Nested
    inner class MatchesIpPatternsTest {
        @Test
        fun `should match exact IPv4`() {
            "192.168.1.1".matchesIpPatterns(listOf("192.168.1.1")) shouldBe true
        }

        @Test
        fun `should match IPv4 with wildcard`() {
            "192.168.1.100".matchesIpPatterns(listOf("192.168.1.*")) shouldBe true
            "192.168.2.100".matchesIpPatterns(listOf("192.168.1.*")) shouldBe false
            "103.215.74.60".matchesIpPatterns(listOf("103.215.7*.*")) shouldBe true
        }

        @Test
        fun `should match exact IPv6`() {
            "2001:db8::1".matchesIpPatterns(listOf("2001:db8::1")) shouldBe true
            "2001:db8:0:0:0:0:0:1".matchesIpPatterns(listOf("2001:db8::1")) shouldBe true
        }

        @Test
        fun `should match IPv6 with wildcard`() {
            "2001:db8::1".matchesIpPatterns(listOf("2001:db8:**")) shouldBe true
            "2001:db8:abcd::1".matchesIpPatterns(listOf("2001:db8:**")) shouldBe true
            "2001:ffff::1".matchesIpPatterns(listOf("2001:db8:**")) shouldBe false
        }
    }

    @Nested
    inner class IsLoopbackAddressTest {
        @Test
        fun `should identify loopback addresses`() {
            "localhost".isLoopbackAddress shouldBe true
            "127.0.0.1".isLoopbackAddress shouldBe true
            "127.0.0.2".isLoopbackAddress shouldBe true
            "::1".isLoopbackAddress shouldBe true
            "192.168.1.1".isLoopbackAddress shouldBe false
        }
    }
}
