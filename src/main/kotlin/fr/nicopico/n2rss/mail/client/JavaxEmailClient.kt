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

import fr.nicopico.n2rss.models.Email
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.search.FlagTerm
import java.util.*

class JavaxEmailClient(
    private val protocol: String,
    private val host: String,
    private val port: Int,
    private val user: String,
    private val password: String,
    private val inboxFolder: String,
) : EmailClient {

    private val props = Properties().apply {
        setProperty("mail.store.protocol", protocol)
    }

    override fun markAsRead(email: Email) {
        doOnInbox {
            open(Folder.READ_WRITE)
            val msg = getMessage(email.msgnum)
            msg.setFlag(Flags.Flag.SEEN, true)
        }
    }

    override fun checkEmails(): List<Email> {
        return doOnInbox {
            open(Folder.READ_ONLY)
            search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                .map(Message::toEmail)
        }
    }

    private fun <T> doOnInbox(block: Folder.() -> T): T {
        val session = Session.getInstance(props, null)
        return session.getStore(protocol).use { store ->
            store.connect(host, port, user, password)
            store.getFolder(inboxFolder).use { inbox ->
                inbox.block()
            }
        }
    }
}
