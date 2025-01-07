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

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.mail.Flags
import jakarta.mail.search.FlagTerm
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JavaxEmailClientTest : GreenMailTestBase(
    userEmail = USER_EMAIL,
    userPassword = USER_PASSWORD,
) {

    companion object {
        private const val INBOX_FOLDER = "INBOX"
        private const val OTHER_FOLDER = "OTHER"
        private const val USER_EMAIL = "user@example.com"
        private const val USER_PASSWORD = "secret"
    }

    private lateinit var emailClient: EmailClient

    @BeforeEach
    fun setUp() {
        val user = greenMail.userManager.getUserByEmail(USER_EMAIL)
        emailClient = JavaxEmailClient(
            protocol = "imap",
            host = greenMail.imap.bindTo,
            port = greenMail.imap.port,
            user = user.email,
            password = user.password,
            folders = listOf(INBOX_FOLDER, OTHER_FOLDER),
        )
    }

    @Test
    fun `emailClient should retrieve a list of unread emails from the inbox`() {
        // GIVEN
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@email.com",
            subject = "Subject 1",
            content = "Hello World! 1",
        )
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@another-email.com",
            subject = "Subject 2",
            content = "Hello World! 2",
        )
        deliverTextMessage(
            folderName = OTHER_FOLDER,
            from = "from@another-email.com",
            subject = "Subject 3",
            content = "Hello World! 3",
        )

        // WHEN
        val emails = emailClient.checkEmails()

        // THEN
        emails shouldHaveSize 3

        assertSoftly(emails[0]) {
            it.sender.sender shouldBe "from@email.com"
            it.subject shouldBe "Subject 1"
            it.content shouldBe "Hello World! 1"
        }

        assertSoftly(emails[1]) {
            it.sender.sender shouldBe "from@another-email.com"
            it.subject shouldBe "Subject 2"
            it.content shouldBe "Hello World! 2"
        }

        assertSoftly(emails[2]) {
            it.sender.sender shouldBe "from@another-email.com"
            it.subject shouldBe "Subject 3"
            it.content shouldBe "Hello World! 3"
        }

        checkMailFolder(INBOX_FOLDER) { folder ->
            folder.search(FlagTerm(Flags(Flags.Flag.SEEN), false)) shouldHaveSize 2
        }
        checkMailFolder(OTHER_FOLDER) { folder ->
            folder.search(FlagTerm(Flags(Flags.Flag.SEEN), false)) shouldHaveSize 1
        }
    }

    @Test
    fun `emailClient should mark message as read`() {
        // GIVEN
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@email.com",
            subject = "Subject 1",
            content = "Hello World! 1",
        )
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@another-email.com",
            subject = "Subject 2",
            content = "Hello World! 2",
        )
        deliverTextMessage(
            folderName = INBOX_FOLDER,
            from = "from@another-email.com",
            subject = "Subject 3",
            content = "Hello World! 3",
        )
        deliverTextMessage(
            folderName = OTHER_FOLDER,
            from = "from@another-email.com",
            subject = "Subject 4",
            content = "Hello World! 4",
        )

        // WHEN - THEN
        val emails = emailClient.checkEmails()

        // THEN
        shouldNotThrowAny {
            emailClient.markAsRead(emails[1])
            emailClient.markAsRead(emails[3])
        }
        val unreadEmails = emailClient.checkEmails()
        unreadEmails.map { it.subject } shouldBe listOf("Subject 1", "Subject 3")
    }
}
