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

package fr.nicopico.n2rss.external.temporary

import com.ninjasquad.springmockk.MockkBean
import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointEntity
import fr.nicopico.n2rss.external.temporary.data.TemporaryEndpointRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@ExtendWith(MockKExtension::class)
@ExtendWith(SpringExtension::class)
@WebMvcTest(TemporaryEndpointController::class)
class TemporaryEndpointControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var temporaryEndpointRepository: TemporaryEndpointRepository

    @Test
    fun `temp-endpoint should return the HTML content`() {
        // GIVEN
        val endpointId = UUID.randomUUID()
        val expectedHtmlContent = "<html><body>Content</body></html>"

        every { temporaryEndpointRepository.findByExposedId(endpointId) } returns TemporaryEndpointEntity(
            exposedId = endpointId,
            label = "",
            content = expectedHtmlContent,
        )

        // WHEN & THEN
        mockMvc.perform(
            get("/temp-endpoint/$endpointId")
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(content().string(expectedHtmlContent))
    }

    @Test
    fun `temp-endpoint should return a 404 for unknown id`() {
        // GIVEN
        val endpointId = UUID.randomUUID()

        every { temporaryEndpointRepository.findByExposedId(any()) } returns null

        // WHEN & THEN
        mockMvc.perform(
            get("/temp-endpoint/$endpointId")
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isNotFound)
    }
}
