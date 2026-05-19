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

import jakarta.servlet.http.HttpServletRequest
import org.springframework.util.AntPathMatcher
import java.net.Inet4Address
import java.net.InetAddress

private val matcher = AntPathMatcher()

fun HttpServletRequest.getClientIp(trustedProxies: List<String> = emptyList()): String {
    val xForwardedFor: String? = getHeader("X-Forwarded-For")
    val useXForwardedFor = !xForwardedFor.isNullOrBlank() &&
        trustedProxies.isNotEmpty() && remoteAddr.matchesIpPatterns(trustedProxies)

    return if (useXForwardedFor) {
        xForwardedFor.split(",").first().trim()
    } else {
        remoteAddr
    }
}

fun String.matchesIpPatterns(patterns: List<String>): Boolean {
    val normalizedIp = this.normalizeForAnt()
    return patterns.any { pattern ->
        matcher.match(pattern.normalizeForAnt(), normalizedIp)
    }
}

private fun String.normalizeForAnt(): String {
    return this.expandIp()
        .replace('.', '/')
        .replace(':', '/')
}

@Suppress("MagicNumber", "ReturnCount")
private fun String.expandIp(): String {
    // Use InetAddress to expand and normalize valid IPv6 addresses
    return try {
        val addr = InetAddress.getByName(this)
        if (addr is Inet4Address) {
            return addr.hostAddress
        }
        val bytes = addr.address
        if (bytes.size == 16) {
            (0..<8).joinToString(":") { i ->
                val b1 = bytes[i * 2].toInt() and 0xff
                val b2 = bytes[i * 2 + 1].toInt() and 0xff
                (b1 shl 8 or b2).toString(16)
            }
        } else this
    } catch (_: Exception) {
        // Fallback for patterns containing wildcards (* or **)
        this.manualExpandIp()
    }
}

@Suppress("MagicNumber", "ReturnCount")
private fun String.manualExpandIp(): String {
    val ipWithGapExpanded = if (this.contains("::")) {
        val parts = this.split("::")
        if (parts.size != 2) return this

        val before = if (parts[0].isEmpty()) emptyList() else parts[0].split(":")
        val after = if (parts[1].isEmpty()) emptyList() else parts[1].split(":")
        val missingCount = 8 - (before.size + after.size)
        if (missingCount < 0) return this

        val gap = List(missingCount) { "0" }
        (before + gap + after).joinToString(":")
    } else {
        this
    }

    val segments = ipWithGapExpanded.split(":")
    return segments.joinToString(":") { segment ->
        if (segment == "*" || segment == "**") segment
        else segment.lowercase().trimStart('0').ifEmpty { "0" }
    }
}

private val IP_PATTERN = Regex("^(?=.*[.:])[0-9a-fA-F.:%]+$")

@Suppress("MagicNumber")
val String.isLoopbackAddress: Boolean
    get() = when {
        this.equals("localhost", ignoreCase = true) -> true
        this.matches(IP_PATTERN) -> {
            runCatching { InetAddress.getByName(this).isLoopbackAddress }
                .getOrDefault(false)
        }
        else -> false
    }
