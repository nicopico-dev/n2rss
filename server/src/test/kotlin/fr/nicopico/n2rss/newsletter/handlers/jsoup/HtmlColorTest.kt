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
package fr.nicopico.n2rss.newsletter.handlers.jsoup

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HtmlColorTest {

    @Test
    fun `should throw on construction with invalid values`() {
        shouldThrow<IllegalArgumentException> {
            HtmlColor(red = 0, green = 260, blue = 0)
        }

        shouldThrow<IllegalArgumentException> {
            HtmlColor(red = -1, green = 0, blue = 0)
        }

        shouldThrow<IllegalArgumentException> {
            HtmlColor(red = 0, green = 0, blue = 300)
        }
    }

    @Nested
    inner class ParseColor {
        @Test
        fun `should create HtmlColor from full hex-color`() {
            // GIVEN
            val hexColor = "#FF0041"

            // WHEN
            val htmlColor = HtmlColor.of(hexColor)

            // THEN
            htmlColor shouldBe HtmlColor(red = 255, green = 0, blue = 65)
        }

        @Test
        fun `should create HtmlColor from short hex-color`() {
            // GIVEN
            val hexColor = "#F04"

            // WHEN
            val htmlColor = HtmlColor.of(hexColor)

            // THEN
            htmlColor shouldBe HtmlColor(red = 255, green = 0, blue = 68)
        }

        @Test
        fun `should create HtmlColor from rgb-color`() {
            // GIVEN
            val rgbColor = "rgb(153, 42, 21)"

            // WHEN
            val htmlColor = HtmlColor.of(rgbColor)

            // THEN
            htmlColor shouldBe HtmlColor(red = 153, green = 42, blue = 21)
        }
    }

    @Nested
    inner class StyleExtraction {
        @Test
        fun `should extract hex-color from simple style`() {
            // GIVEN
            val style = "color: #257953"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor shouldBe HtmlColor.of("#257953")
        }

        @Test
        fun `should extract hex-color from complex style`() {
            // GIVEN
            val style = "style: font-family: Whyte, Helvetica, Arial, sans-serif; color: #257953"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor shouldBe HtmlColor.of("#257953")
        }

        @Test
        fun `should extract hex-color from complex style 2`() {
            // GIVEN
            val style =
                "style: font-family: Whyte, Helvetica, Arial, sans-serif; color: #00FF32; text-decoration: underline"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor shouldBe HtmlColor.of("#00FF32")
        }

        @Test
        fun `should extract rgb-color from simple style`() {
            // GIVEN
            val style = "color: rgb(255, 0, 255)"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor shouldBe HtmlColor(red = 255, green = 0, blue = 255)
        }

        @Test
        fun `should extract rgb-color from complex style`() {
            // GIVEN
            val style = "font-family: Whyte, Helvetica, Arial, sans-serif; color: rgb(255, 0, 255)"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor shouldBe HtmlColor(red = 255, green = 0, blue = 255)
        }

        @Test
        fun `should extract rgb-color from complex style 2`() {
            // GIVEN
            val style =
                "font-family: Whyte, Helvetica, Arial, sans-serif; color: rgb(37, 121, 83); text-decoration: none"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor shouldBe HtmlColor(red = 37, green = 121, blue = 83)
        }

        @Test
        fun `should return null if the style does not contain color`() {
            // GIVEN
            val style = "text-decoration: none"

            // WHEN
            val htmlColor = HtmlColor.extractFromStyle(style)

            // THEN
            htmlColor should beNull()
        }
    }

    @Nested
    inner class ColorMatch {
        @Test
        fun `should match exact color`() {
            // GIVEN
            val reference = HtmlColor(red = 255, green = 40, blue = 8)
            val candidate1 = HtmlColor(red = 255, green = 40, blue = 8)
            val candidate2 = HtmlColor(red = 123, green = 23, blue = 10)

            // WHEN
            val match1 = reference.matches(candidate1)
            val match2 = reference.matches(candidate2)

            // THEN
            match1 shouldBe true
            match2 shouldBe false
        }

        @Test
        fun `should match within tolerance color`() {
            // GIVEN
            val reference = HtmlColor(red = 255, green = 40, blue = 8)
            val candidate1 = HtmlColor(red = 255, green = 40, blue = 8)
            val candidate2 = HtmlColor(red = 254, green = 42, blue = 10)
            val candidate3 = HtmlColor(red = 123, green = 23, blue = 10)

            // WHEN
            val match1 = reference.matches(candidate1, tolerance = 5)
            val match2 = reference.matches(candidate2, tolerance = 5)
            val match3 = reference.matches(candidate3, tolerance = 5)

            // THEN
            match1 shouldBe true
            match2 shouldBe true
            match3 shouldBe false
        }
    }
}
