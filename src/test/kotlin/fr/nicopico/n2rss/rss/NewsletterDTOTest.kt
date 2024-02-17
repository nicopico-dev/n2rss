package fr.nicopico.n2rss.rss

import com.fasterxml.jackson.databind.ObjectMapper
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
            publicationCount = publicationCount,
        )

        // WHEN
        val json = objectMapper.writeValueAsString(original)

        // THEN
        val dto = objectMapper.readValue(json, NewsletterDTO::class.java)
        dto shouldBe original
    }
}
