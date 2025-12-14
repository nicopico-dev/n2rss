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

import jakarta.mail.Message
import kotlinx.datetime.LocalDate

data class Email(
    val sender: Sender,
    val date: LocalDate,
    val subject: String,
    val content: EmailContent,
    val replyTo: Sender? = null,
) {
    // `message` is used as an identifier to operate on the email with Jakarta Mail API.
    // `underlyingMessage` is kept outside the primary constructor
    // as multiple reads of the same emails could be unequal
    @Suppress("DataClassShouldBeImmutable")
    private lateinit var underlyingMessage: Message

    @Synchronized
    fun setMessage(message: Message) {
        check(!this::underlyingMessage.isInitialized) {
            "Message already set"
        }
        underlyingMessage = message
    }

    val message: Message
        get() = underlyingMessage
}
