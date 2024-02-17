package fr.nicopico.n2rss.utils

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.jupiter.api.Test
import java.util.*

class DateExtKtTest {

    @Test
    fun `LocalDate should convert to legacy date`() {
        // GIVEN
        val year = 2021
        val monthNumber = 8
        val dayOfMonth = 3
        val localDate = LocalDate(year, monthNumber, dayOfMonth)

        // WHEN
        val legacyDate: Date = localDate.toLegacyDate()

        // THEN
        @Suppress("DEPRECATION")
        assertSoftly {
            legacyDate.year shouldBe (year - 1900)
            legacyDate.month shouldBe (monthNumber - 1)
            legacyDate.day shouldBe (dayOfMonth - 1)
        }
    }

    @Test
    fun `Legacy date should convert to LocalDate`() {
        // GIVEN
        val year = 2042
        val month = 6
        val dayOfMonth = 21
        @Suppress("DEPRECATION")
        val date = Date(
            year - 1900,   // since year 0 in Date class refers to 1900
            month - 1,        // 0-based month
            dayOfMonth,
        )

        // WHEN
        val localDate = date.toKotlinLocaleDate()

        // THEN
        assertSoftly {
            localDate.year shouldBe (year)
            localDate.month shouldBe Month.JUNE
            localDate.dayOfMonth shouldBe (dayOfMonth)
        }
    }
}
