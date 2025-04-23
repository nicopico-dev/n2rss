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

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.net.URI

class RestClientExtKtTest {

    @Test
    fun `should resolve an URI with 302 redirect`() {
        val newsletterUri: URI = URI.create(
            "https://f6p7au8ab.cc.rs6.net/tn.jsp?f=001DOzVYeoi_U8vAlnJAn9bZ-QTgRI4Civmiz1NoNpWDzi1oKjl0VNGULdyb96qdfHWspriSkrhat5m9DQax_Jk849PRYhVw7RmNYbvio2HCsW0H-35rJWcu7j1ItF4Rl0FOkjNKMyjGmRbbCmhi-Ec0QDlnSqGVte5dlxLLqVW9bc1vdCQ3qtZeoqOvvX5y8qFTXfjl-4DagA=&c=Q9c5o8mt_8OViXlfmsJkqA6CorbOewgTT4JDqd720cnkPFc8CUj-WA==&ch=7Dq-GqBxSzH5npeoV9hdFigIdzmKY1iDKFdziUVFscR07n774bwUUw=="
        )
        val resolvedUri = URI.create("https://carrion.dev/en/posts/context-parameters-kotlin/")

        val result = runBlocking {
            resolveUris(listOf(newsletterUri))
        }

        result shouldBe mapOf(newsletterUri to resolvedUri)
    }
}
