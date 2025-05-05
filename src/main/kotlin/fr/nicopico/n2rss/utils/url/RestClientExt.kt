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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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

private const val TIMEOUT_MS = 5000
private val LOG = LoggerFactory.getLogger("RestClientExt")

suspend fun resolveUris(
    userAgent: String,
    urls: List<URI>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    maxConcurrency: Int = 5,
): Map<URI, URI?> {
    val restClient = RestClient.builder()
        .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
        .requestFactory(
            object : SimpleClientHttpRequestFactory() {
                override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                    super.prepareConnection(connection, httpMethod)
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = TIMEOUT_MS
                    connection.readTimeout = TIMEOUT_MS
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
                        withTimeout(TIMEOUT_MS.toLong()) {
                            restClient.resolveUrl(articleURI)
                        }
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

private suspend fun RestClient.resolveUrl(originalUri: URI): Deferred<URI?> = coroutineScope {
    async {
        suspendCancellableCoroutine {
            // TODO Add support for beehiiv URLs
            if (originalUri.host == "link.mail.beehiiv.com") {
                // beehiiv URLs are not yet supported
                it.resume(null)
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
                        LOG.error("Invalid location header in response to GET $originalUri", e)
                        null
                    }

                response.statusCode.isError -> null
                else -> originalUri
            }

            it.resume(resolvedUri)
        }.let { resolvedUri ->
            // Handle multiple redirects
            if (resolvedUri != null && resolvedUri != originalUri) {
                resolveUrl(resolvedUri).await()
            } else resolvedUri
        }
    }
}
