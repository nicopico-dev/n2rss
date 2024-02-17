package fr.nicopico.n2rss.utils

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.net.URL

class UrlExtKtTest {

    @Test
    fun `Valid url should convert to URL object`() {
        // GIVEN
        val urlString = "https://www.example.com"

        // WHEN
        val result = urlString.toURL()

        // THEN
        assertSoftly {
            result shouldNotBe null
            result?.protocol shouldBe "https"
            result?.host shouldBe "www.example.com"
        }
    }

    @Test
    fun `Invalid url should return null`() {
        // GIVEN
        val urlString: String = "invalid_url"

        // WHEN
        val result: URL? = urlString.toURL()

        // THEN
        result shouldBe null
    }
}
