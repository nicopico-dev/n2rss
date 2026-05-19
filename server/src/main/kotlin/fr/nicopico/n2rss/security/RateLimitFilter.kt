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

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.time.Duration.Companion.nanoseconds

private val LOG = LoggerFactory.getLogger(RateLimitFilter::class.java)

@Component
class RateLimitFilter(
    private val rateLimiterService: RateLimiterService,
    @param:Value($$"${n2rss.security.trusted-proxies}")
    private val trustedProxies: List<String>,
    @param:Value($$"${n2rss.security.rate-limiting-enabled}")
    private val rateLimitingEnabled: Boolean,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!rateLimitingEnabled || request.remoteAddr.isLoopbackAddress) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = request.getClientIp(trustedProxies)
        val bucket = rateLimiterService.resolveBucket(clientIp)

        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (!probe.isConsumed) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "text/plain"
            response.setHeader(
                "Retry-After",
                (probe.nanosToWaitForRefill.nanoseconds.inWholeSeconds + 1).toString(),
            )
            response.writer.write("Too many requests")
            LOG.warn("Refused request from IP {} (too many request): {}", clientIp, request)
            return
        }

        response.setHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
        filterChain.doFilter(request, response)
    }
}
