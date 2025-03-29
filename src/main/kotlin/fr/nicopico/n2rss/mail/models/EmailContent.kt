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
package fr.nicopico.n2rss.mail.models

sealed class EmailContent {
    data class TextOnly(
        val text: String
    ) : EmailContent()

    data class TextAndHtml(
        val text: String,
        val html: String,
    ) : EmailContent()

    data class HtmlOnly(
        val html: String
    ) : EmailContent()
}

val EmailContent.text: String
    get() = textOrNull
        ?: throw IllegalArgumentException("$this does not have text content")

val EmailContent.html: String
    get() = htmlOrNull
        ?: throw IllegalArgumentException("$this does not have html content")

val EmailContent.textOrNull: String?
    get() = when (this) {
        is EmailContent.TextAndHtml -> text
        is EmailContent.TextOnly -> text
        is EmailContent.HtmlOnly -> null
    }

val EmailContent.htmlOrNull: String?
    get() = when (this) {
        is EmailContent.HtmlOnly -> html
        is EmailContent.TextAndHtml -> html
        is EmailContent.TextOnly -> null
    }
