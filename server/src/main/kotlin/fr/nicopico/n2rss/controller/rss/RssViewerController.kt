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
package fr.nicopico.n2rss.controller.rss

import fr.nicopico.n2rss.config.N2RssProperties
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

private val LOG = LoggerFactory.getLogger(RssViewerController::class.java)

@Controller
class RssViewerController(
    private val feedProperties: N2RssProperties.FeedsProperties,
) {

    @GetMapping("rss/viewer")
    fun feedViewer(
        request: HttpServletRequest,
        model: Model,
        @RequestParam(value = "code") code: String,
        @Suppress("unused") /* Used by AnalyticsAspect */
        @RequestHeader(value = "User-Agent") userAgent: String,
    ): String {
        val hostname = request.getHeader("Host") ?: request.serverName
        val scheme = if (feedProperties.forceHttps) "https" else request.scheme
        val feedUrl = "$scheme://$hostname/rss/$code"

        LOG.debug("Opening RSS viewer for url: {}", feedUrl)

        with(model) {
            addAttribute("feedUrl", feedUrl)
        }

        return "rss-viewer"
    }
}
