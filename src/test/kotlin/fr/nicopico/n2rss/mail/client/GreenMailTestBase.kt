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

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import jakarta.mail.Flags
import jakarta.mail.Folder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension

open class GreenMailTestBase(
    private val userEmail: String = "user@example.com",
    private val userPassword: String = "secret",
) {

    companion object {
        private val serverSetup = ServerSetup.IMAP
            .dynamicPort()
            .verbose(false)

        @JvmField
        @RegisterExtension
        val greenMail = GreenMailExtension(serverSetup)
    }

    private lateinit var mailSession: jakarta.mail.Session

    @BeforeEach
    fun setUpBase() {
        greenMail.setUser(userEmail, userPassword)
        mailSession = greenMail.imap.createSession()
    }

    protected fun deliverTextMessage(folderName: String, from: String, subject: String, content: String) {
        val message = GreenMailUtil.createTextEmail(
            userEmail,
            from,
            subject,
            content,
            serverSetup
        )
        message.setFlags(Flags(Flags.Flag.SEEN), false)

        mailSession.getStore(serverSetup.protocol).use { store ->
            store.connect(userEmail, userPassword)
            store.getFolder(folderName).use { folder ->
                if (!folder.exists()) {
                    folder.create(Folder.HOLDS_FOLDERS or Folder.HOLDS_MESSAGES)
                }
                folder.open(Folder.READ_WRITE)
                folder.appendMessages(arrayOf(message))
            }
        }
    }

    protected fun checkMailFolder(folderName: String, block: (Folder) -> Unit) {
        mailSession.getStore(serverSetup.protocol).use { store ->
            store.connect(userEmail, userPassword)
            store.getFolder(folderName).use { folder ->
                folder.open(Folder.READ_ONLY)
                block(folder)
            }
        }
    }
}
