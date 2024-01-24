package fr.nicopico.n2rss.mail.newsletter

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.EntrySource
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.kotlinx.datetime.shouldHaveSameDayAs
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

class AndroidWeeklyNewsletterHandlerTest {

    private lateinit var handler: AndroidWeeklyNewsletterHandler

    @BeforeEach
    fun setUp() {
        handler = AndroidWeeklyNewsletterHandler()
    }

    @Nested
    inner class CanHandleTest {
        @Test
        fun `should handle all emails from AndroidWeekly`() {
            // GIVEN
            val emails = loadEmails("emails/Android Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe true
        }

        @Test
        fun `should ignore all emails from another newsletters`() {
            // GIVEN
            val emails = loadEmails("emails/Kotlin Weekly")

            // WHEN - THEN
            emails.all { handler.canHandle(it) } shouldBe false
        }
    }

    @Nested
    inner class ProcessTest {
        @Test
        fun `should extract all articles from an Android Weekly email`() {
            // GIVEN
            val email: Email = loadEmail("emails/Android Weekly/Android Weekly #605.eml")

            // WHEN
            val entries = handler.process(email)

            // THEN
            entries shouldHaveSize 9
            entries.map { it.title } shouldBe listOf(
                "How to add text similarity easily using MediaPipe and Kotlin",
                "Safely Navigating the Transition: From Gson to kotlinx.serialization",
                "Creating and managing custom-scoped components in Dagger + Anvil",
                "Camouflage the Status Bar with Edge-to-Edge Jetpack Compose Dialogs",
                "Why use Flow if we have the powerful ChannelFlow in mobile development?",
                "Exploring Health Connect Pt. 1 - Setting Up Permissions",
                "Running UI tests in Jetpack Compose using Firebase Test Lab",
                "Kotlin-Swift interopedia",
                "Keep Your Kotlin Code Spotless: A Guide to ktlint and ktfmt Linters",
            )

            entries[0] should { entry ->
                entry.title shouldBe "How to add text similarity easily using MediaPipe and Kotlin"
                entry.link shouldBe URL("https://r20.rs6.net/tn.jsp?f=001YHdDWcIocRmEUng2FyDIZCM865jNXQCI2Rqy-2M-vNbfJdIgMHvRtHYbATstN3kGB9HprhnbPYLVzFxoewUaaejGyLe52auChGpRvDynAVEWc1A1wSoy8ZmiqmqNJhMuf67y5eG7ydf1OTZPZPB4FPQwqWT2LK-6hXWhyH0Js9DME5DujnMrIvbnalUKlch86KDIzPj-UlGsMphBYKA9IkE2nX46rr-9gj_u2vZ0H8syGAECFgg4qV3ZXxfCkGe84XXhoCcTWt8ZhrNzT0JJEx5rz84rYNdXtCOSyPEJHNKOYDOg9-X6Zg==&c=zrzdfNBtlG2zX5Iq8EiYBmRjj1u-3dLoDyskVZ8jzgoLL4bugqTz3Q==&ch=b-W9Yhs6lzvdh-BOht77aRzv1XT0juBV2IBbXPI7zM490y0EfdXTUA==")
                entry.description shouldBe "Juan Guillermo Gómez Torres takes a look at MediaPipe Solutions, which provides a set of libraries and tools to apply machine learning (ML) to your applications quickly."
                entry.pubDate.shouldHaveSameDayAs(email.date)
                entry.source shouldBe EntrySource(
                    handler = AndroidWeeklyNewsletterHandler::class,
                    title = "Android Weekly #605 \uD83E\uDD16"
                )
            }
        }
    }
}
