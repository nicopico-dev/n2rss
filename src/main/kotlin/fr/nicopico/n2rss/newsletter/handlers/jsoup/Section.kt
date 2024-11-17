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
package fr.nicopico.n2rss.newsletter.handlers.jsoup

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

data class Section(
    val title: String,
    val start: Element,
    val end: Element? = null,
)

fun Document.extractSections(
    cssQuery: String,
    filter: (Element) -> Boolean = { true },
    getSectionTitle: (Element) -> String = { it.text() },
): List<Section> {
    val sectionsElements = select(cssQuery).filter(filter)
    val commonAncestor = sectionsElements.findCommonAncestor()
    return sectionsElements
        .mapIndexed { index, element ->
            val nextSectionElement = sectionsElements
                .getOrNull(index + 1)

            val start = element.parents().first { it.parent() == commonAncestor }
            val end = nextSectionElement?.let {
                it.parents().first { p -> p.parent() == commonAncestor }
            }

            Section(
                title = getSectionTitle(element),
                start = start,
                end = end,
            )
        }
}

private fun Iterable<Element>.findCommonAncestor(): Element {
    return fold(
        initial = first().parents().toSet(),
        operation = { commonAncestors, element ->
            commonAncestors.intersect(element.parents())
        }
    ).first()
}

fun findStartAndEnd(element: Element, otherElement: Element?): Pair<Element, Element?> {
    return if (otherElement == null) {
        element to null
    } else {
        val commonAncestor = element.parents().intersect(otherElement.parents()).first()
        val start = element.parents().first { it.parent() == commonAncestor }
        val end = otherElement.parents().first { it.parent() == commonAncestor }

        start to end
    }
}

inline fun <T> Section.process(block: (section: Document) -> T): T {
    val section = Document("").apply {
        val nodesBetween = (start.parent()?.childNodes().orEmpty())
            .dropWhile { it != start }
            .takeWhile { it != end }
        appendChildren(nodesBetween)
    }
    return block(section)
}
