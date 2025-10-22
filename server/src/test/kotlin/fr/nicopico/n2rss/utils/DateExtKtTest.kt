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

package fr.nicopico.n2rss.utils

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.jupiter.api.Test
import java.util.Date

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
            month - 1,    // 0-based month
            dayOfMonth,
        )

        // WHEN
        val localDate = date.toKotlinLocaleDate()

        // THEN
        assertSoftly {
            localDate.year shouldBe (year)
            localDate.month shouldBe Month.JUNE
            localDate.day shouldBe (dayOfMonth)
        }
    }
}
