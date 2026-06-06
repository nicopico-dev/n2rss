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
import fr.nicopico.n2rss.analytics.models.AnalyticsException
import fr.nicopico.n2rss.analytics.service.AnalyticsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val LOG = LoggerFactory.getLogger(IpBlockerFilter::class.java)

@Component
class IpBlockerFilter(
    @Value($$"${n2rss.security.blocked-ip-patterns}")
    blockedIpPatterns: List<String>,
    @param:Value($$"${n2rss.security.trusted-proxies}")
    private val trustedProxies: List<String>,
    private val analyticsService: AnalyticsService,
) : OncePerRequestFilter() {

    private val blockedIpAntPatterns: List<String> = blockedIpPatterns
        .filter { it.isNotBlank() }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp = request.getClientIp(trustedProxies)
        if (clientIp.matchesIpPatterns(blockedIpAntPatterns)) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            LOG.warn("Refused request for permanently blocked IP {}: {}", clientIp, request.requestURI)
            trackBlockedIpRequest(request, clientIp)
            return
        } else {
            LOG.debug("Request accepted for IP {}: {}", clientIp, request.requestURI)
        }

        filterChain.doFilter(request, response)
    }

    private fun trackBlockedIpRequest(request: HttpServletRequest, clientIp: String) {
        try {
            val userAgent = request.getHeader("User-Agent") ?: ""
            val requestedUrl = request.requestURI
            analyticsService.track(
                AnalyticsEvent.BlockedIpRequestEvent(
                    userAgent = userAgent,
                    clientIpAddress = clientIp,
                    requestedUrl = requestedUrl
                )
            )
        } catch (e: AnalyticsException) {
            LOG.error("Could not track BlockedIpRequestEvent", e)
        }
    }
}
