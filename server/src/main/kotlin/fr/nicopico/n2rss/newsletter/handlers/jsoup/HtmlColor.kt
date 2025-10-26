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

import org.slf4j.LoggerFactory

data class HtmlColor(
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    init {
        require(
            red in 0..COLOR_MAX_VALUE
                && green in 0..COLOR_MAX_VALUE
                && blue in 0..COLOR_MAX_VALUE
        ) {
            "colors must be between 0 and 255 : ($red, $green, $blue)"
        }
    }

    fun matches(color: HtmlColor): Boolean {
        val delta =
            (red - color.red) +
                (green - color.green) +
                (blue - color.blue)
        // TODO Add a tolerance
        return delta == 0
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HtmlColor::class.java)

        private const val COLOR_MAX_VALUE = 255

        private val hexColorRegex = Regex("#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{3}")
        private val rgbColorRegex = Regex("rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)")
        private val styleColorRegex = Regex("color:\\s*(.+?)(?:;|$)")

        fun of(value: String): HtmlColor {
            return when {
                hexColorRegex.matches(value) -> {
                    when (value.length) {
                        // 2 digits per color
                        7 -> HtmlColor(
                            red = value.substring(1, 3).hexToInt(),
                            green = value.substring(3, 5).hexToInt(),
                            blue = value.substring(5, 7).hexToInt(),
                        )

                        // 1 digits per color
                        4 -> HtmlColor(
                            red = value[1].toString().repeat(2).hexToInt(),
                            green = value[2].toString().repeat(2).hexToInt(),
                            blue = value[3].toString().repeat(2).hexToInt(),
                        )

                        else -> error("Invalid hex color : $value")
                    }
                }

                rgbColorRegex.matches(value) -> {
                    val match = rgbColorRegex.matchEntire(value)!!
                    HtmlColor(
                        red = match.groupValues[1].toInt(),
                        green = match.groupValues[2].toInt(),
                        blue = match.groupValues[3].toInt()
                    )
                }

                else -> error("Unsupported color : $value")
            }
        }

        fun extractFromStyle(style: String): HtmlColor? {
            return styleColorRegex.find(style)?.let { colorValue ->
                try {
                    @Suppress("RemoveRedundantQualifierName")
                    HtmlColor.of(colorValue.groupValues[1])
                } catch (e: IllegalStateException) {
                    LOG.warn(e.message)
                    null
                }
            }
        }
    }
}
