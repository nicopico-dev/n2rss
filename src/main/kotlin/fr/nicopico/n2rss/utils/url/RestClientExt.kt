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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.HttpURLConnection
import java.net.URI
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

private val LOG = LoggerFactory.getLogger("RestClientExt")

suspend fun resolveUris(
    urls: List<URI>,
    userAgent: String = "n2rss",
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    maxConcurrency: Int = 5,
    timeout: Duration = 5000.milliseconds,
): Map<URI, URI?> {
    val restClient = RestClient.builder()
        .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
        .requestFactory(
            object : SimpleClientHttpRequestFactory() {
                override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                    super.prepareConnection(connection, httpMethod)
                    connection.instanceFollowRedirects = false

                    // Use `timeout + 1` to trigger Kotlin Coroutines withTimeout
                    val timeoutMs = timeout.toInt(DurationUnit.MILLISECONDS)
                    connection.connectTimeout = timeoutMs + 1
                    connection.readTimeout = timeoutMs + 1
                }

                override fun createRequest(uri: URI, httpMethod: HttpMethod): ClientHttpRequest {
                    if (!uri.isAbsolute) {
                        LOG.warn("Non-absolute URI '{}' are not supported!", uri)
                    }
                    return super.createRequest(uri, httpMethod)
                }
            }
        )
        .build()

    return withContext(dispatcher) {
        urls.chunked(maxConcurrency)
            .flatMap { urlChunk ->
                val resolvedUrls = urlChunk
                    .associateWith { articleURI ->
                        restClient.resolveUrl(articleURI, timeout)
                    }
                resolvedUrls.values.awaitAll()
                resolvedUrls.entries
            }
            .associate {
                it.key to it.value.await()
            }
    }
}

private val REDIRECT_STATUS_CODES = listOf(
    HttpStatus.MOVED_PERMANENTLY,   // 301
    HttpStatus.FOUND,               // 302
    HttpStatus.TEMPORARY_REDIRECT,  // 307
    HttpStatus.PERMANENT_REDIRECT,  // 308
)

private suspend fun RestClient.resolveUrl(
    originalUri: URI,
    timeout: Duration,
): Deferred<URI?> = coroutineScope {
    async {
        val resolvedUri = withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine { continuation ->
                // TODO Add support for beehiiv URLs
                if (originalUri.host == "link.mail.beehiiv.com") {
                    // beehiiv URLs are not yet supported
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                // Call HTTP to resolve the URL
                @Suppress("TooGenericExceptionCaught")
                val response = try {
                    get()
                        .uri(originalUri)
                        .retrieve()
                        .onStatus { httpResponse ->
                            // Ignore HTTP errors
                            if (httpResponse.statusCode.isError) {
                                LOG.warn("HTTP error when resolving url $originalUri -> ${httpResponse.statusCode}")
                            }
                            true
                        }
                        .toBodilessEntity()
                } catch (e: Exception) {
                    continuation.context.ensureActive()
                    LOG.error("Failed to GET $originalUri", e)
                    null
                }

                val resolvedUri = when {
                    response == null -> null
                    response.statusCode in REDIRECT_STATUS_CODES ->
                        @Suppress("TooGenericExceptionCaught")
                        try {
                            response.headers.location
                        } catch (e: Exception) {
                            continuation.context.ensureActive()
                            LOG.error("Invalid location header in response to GET $originalUri", e)
                            null
                        }

                    response.statusCode.isError -> null
                    else -> originalUri
                }

                continuation.resume(resolvedUri)
            }
        }

        // Handle multiple redirects
        if (resolvedUri != null && resolvedUri != originalUri) {
            resolveUrl(resolvedUri, timeout).await()
        } else resolvedUri
    }
}
