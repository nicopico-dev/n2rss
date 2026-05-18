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

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Service
class RateLimiterService(
    @param:Value($$"${n2rss.security.max-requests-per-minute}")
    private val maxRequestPerMinute: Long,
) {

    private val buckets: Cache<String, Bucket> = Caffeine.newBuilder()
        .maximumSize(MAX_CACHE_SIZE)
        .expireAfterAccess(CACHE_DURATION_IN_SECONDS, TimeUnit.SECONDS)
        .build()

    fun resolveBucket(key: String): Bucket {
        return buckets.get(key, ::createBucketForKey)
    }

    private fun createBucketForKey(
        @Suppress("unused")
        key: String
    ): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(maxRequestPerMinute)
                    .refillIntervally(maxRequestPerMinute, 1.minutes.toJavaDuration())
                    .build()
            )
            .build()
    }

    companion object {
        private const val MAX_CACHE_SIZE = 1000L
        private const val CACHE_DURATION_IN_SECONDS = 15 * 60L
    }
}
