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

import com.fasterxml.jackson.databind.ObjectMapper
import fr.nicopico.n2rss.controller.dto.NewsletterDTO
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NewsletterDTOTest {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
    }

    @Test
    fun `NewsletterDTO should serialize to JSON`() {
        // GIVEN
        val code = "adaptor"
        val title = "Myanmar expression rom affecting teaching caught smilies"
        val publicationCount = 2977L

        val original = NewsletterDTO(
            code = code,
            title = title,
            websiteUrl = "https://example.com",
            notes = null,
            publicationCount = publicationCount,
            startingDate = null,
            publicationStats = null,
        )

        // WHEN
        val json = objectMapper.writeValueAsString(original)

        // THEN
        val dto = objectMapper.readValue(json, NewsletterDTO::class.java)
        dto shouldBe original
    }
}
