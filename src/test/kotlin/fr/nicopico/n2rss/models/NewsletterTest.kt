package fr.nicopico.n2rss.models

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class NewsletterTest {

    companion object {
        @JvmStatic
        fun paramProvider(): Stream<Arguments> = Stream.of(
            Arguments.of("Android Weekly", "android_weekly"),
        )
    }

    @ParameterizedTest
    @MethodSource("paramProvider")
    fun `should generate code from name`(name: String, expectedCode: String) {
        // GIVEN
        val newsletter = Newsletter(name, websiteUrl = "")

        // WHEN - THEN
        newsletter.code shouldBe expectedCode
    }
}
