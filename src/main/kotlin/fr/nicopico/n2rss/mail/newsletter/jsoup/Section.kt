/*
 * Copyright (c) 2024 Nicolas PICON
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

package fr.nicopico.n2rss.mail.newsletter.jsoup

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

data class Section(
    val title: String,
    val start: Element,
    val end: Element? = null,
)

fun Document.extractSections(
    cssQuery: String,
    getSectionTitle: (Element) -> String = { it.text() },
): List<Section> {
    val sectionsElements = select(cssQuery)
    return sectionsElements
        .mapIndexed { index, element ->
            val nextSectionElement = sectionsElements
                .getOrNull(index + 1)
            Section(
                title = getSectionTitle(element),
                start = element,
                end = nextSectionElement,
            )
        }
}

inline fun <T> Section.process(block: (section: Document) -> T): T {
    val section = Document("").apply {
        val nodesBetween = (start.parent()?.childNodes() ?: emptyList())
            .dropWhile { it != start }
            .takeWhile { it != end }
        appendChildren(nodesBetween)
    }
    return block(section)
}
