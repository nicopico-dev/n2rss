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
package fr.nicopico.n2rss.mail.client

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import jakarta.mail.Flags
import jakarta.mail.search.FlagTerm
import org.junit.jupiter.api.Test

class MessageExtGreenMailTest : GreenMailTestBase() {

    companion object {
        private const val INBOX_FOLDER = "INBOX"
    }

    @Test
    fun `greenmail should not mark message as seen automatically`() {
        // GIVEN
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@email.com",
            subject = "Subject",
            content = "Hello World!"
        )

        withClue("Check the message is not marked as SEEN") {
            checkMailFolder(INBOX_FOLDER) { folder ->
                folder.search(FlagTerm(Flags(Flags.Flag.SEEN), false)) shouldHaveSize 1
            }
        }

        // WHEN
        checkMailFolder(INBOX_FOLDER) { folder ->
            // Read the content of the message
            val message = folder.messages[0]
            println(">>> BEFORE READ CONTENT")
            println(message.toEmail())
            println(">>> AFTER READ CONTENT")
        }

        // THEN
        withClue("Check the message is still not marked as SEEN after peek") {
            checkMailFolder(INBOX_FOLDER) { folder ->
                folder.search(FlagTerm(Flags(Flags.Flag.SEEN), false)) shouldHaveSize 1
            }
        }
    }
}
