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
package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.mail.models.Email
import fr.nicopico.n2rss.mail.models.Sender
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import jakarta.mail.Message
import jakarta.mail.internet.MimeMultipart

fun Message.toEmail() = Email(
    Sender(from[0].toString()),
    date = this.sentDate.toKotlinLocaleDate(),
    subject = subject,
    content = when (content) {
        is MimeMultipart -> (content as MimeMultipart).getHtmlBodyPart()
        else -> content.toString()
    },
    msgnum = this.messageNumber,
)

private fun MimeMultipart.getHtmlBodyPart(): String {
    val partCount = this.count
    for (i in 0 until partCount) {
        val bodyPart = this.getBodyPart(i)
        if (bodyPart.isMimeType("text/html")) {
            return bodyPart.content as String
        }
    }
    throw NoSuchElementException("no text/html part found in the Message")
}
