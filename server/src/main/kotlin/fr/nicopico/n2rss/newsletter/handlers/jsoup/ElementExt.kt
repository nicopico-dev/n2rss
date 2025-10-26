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

import org.jsoup.nodes.Element

/**
 * Extract text content of the [Element] while keeping line-feeds.
 *
 * @see Element.wholeText
 */
fun Element.textWithLineFeeds(): String {
    return this.wholeText()
        // Remove HTML indentation
        .replace(Regex("[ \t]{2,}"), " ")
        .replace(Regex("^ (\\w)?", RegexOption.MULTILINE), "$1")
        // Keep line-feeds but remove empty lines
        .replace(Regex("\\n{3,}"), "\n\n")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .trim()
}

private val backgroundColorRegex = Regex("background-color\\s*:\\s*(#\\w+)\\b")
val Element.backgroundColor: String?
    get() {
        val style = attr("style")
        return backgroundColorRegex.find(style)?.groupValues?.get(1)
    }
