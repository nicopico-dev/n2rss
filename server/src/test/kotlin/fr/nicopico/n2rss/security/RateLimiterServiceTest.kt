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
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RateLimiterServiceTest {

    private lateinit var rateLimiterService: RateLimiterService
    private val maxRequestsPerMinute = 10L

    @BeforeEach
    fun setUp() {
        rateLimiterService = RateLimiterService(maxRequestsPerMinute)
    }

    @Test
    fun `resolveBucket should return a bucket for a new key`() {
        // GIVEN
        val key = "127.0.0.1"

        // WHEN
        val bucket = rateLimiterService.resolveBucket(key)

        // THEN
        bucket shouldNotBe null
        bucket.availableTokens shouldBe maxRequestsPerMinute
    }

    @Test
    fun `resolveBucket should return the same bucket for the same key`() {
        // GIVEN
        val key = "127.0.0.1"
        val bucket1 = rateLimiterService.resolveBucket(key)

        // WHEN
        val bucket2 = rateLimiterService.resolveBucket(key)

        // THEN
        bucket1 shouldBe bucket2
    }

    @Test
    fun `resolveBucket should return different buckets for different keys`() {
        // GIVEN
        val key1 = "127.0.0.1"
        val key2 = "192.168.1.1"

        // WHEN
        val bucket1 = rateLimiterService.resolveBucket(key1)
        val bucket2 = rateLimiterService.resolveBucket(key2)

        // THEN
        bucket1 shouldNotBe bucket2
    }

    @Test
    fun `bucket should have the correct capacity`() {
        // GIVEN
        val key = "127.0.0.1"
        val bucket = rateLimiterService.resolveBucket(key)

        // WHEN
        val availableTokens = bucket.availableTokens

        // THEN
        availableTokens shouldBe maxRequestsPerMinute
    }
}
