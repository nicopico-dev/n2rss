/*
 * Copyright (c) 2024 Nicolas PICON
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
package fr.nicopico.n2rss.controller

import fr.nicopico.n2rss.utils.UrlValidator
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class UrlValidatorTest {

    private lateinit var urlValidator: UrlValidator

    @BeforeEach
    fun setUp() {
        urlValidator = UrlValidator()
    }

    companion object {
        @JvmStatic
        @Suppress("HttpUrlsUsage")
        fun provideUrls(): List<Arguments> {
            return listOf(
                // Unprovided values are considered valid
                Arguments.of(null, true),
                Arguments.of("", true),
                // HTTP and HTTPS are valid
                Arguments.of("https://www.google.com", true),
                Arguments.of("http://www.android.com", true),
                // Urls without scheme can be valid
                Arguments.of("www.google.com", true),
                Arguments.of("nirvana.com", true),
                // Invalid values
                Arguments.of("mail://test@n2rss.fr", false),
                Arguments.of("test@n2rss.fr", false),
                Arguments.of("Invalid_URL", false),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideUrls")
    fun `test url validator`(url: String?, expected: Boolean) {
        // WHEN
        val result = urlValidator.isValid(url, null)

        // THEN
        result shouldBe expected
    }
}
