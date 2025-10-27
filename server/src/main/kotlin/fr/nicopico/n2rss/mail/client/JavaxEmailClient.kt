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

import fr.nicopico.n2rss.mail.models.Email
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.search.FlagTerm
import java.util.Properties

class JavaxEmailClient(
    private val config: EmailServerConfiguration,
    private val folders: List<String>,
    private val processedFolder: String = "Trash",
) : EmailClient {

    private val props = Properties().apply {
        setProperty("mail.store.protocol", config.protocol)
    }

    override fun markAsRead(email: Email) {
        with(email.messageId) {
            doInFolder(folder) {
                open(Folder.READ_WRITE)
                val msg = getMessage(msgNum)
                msg.setFlag(Flags.Flag.SEEN, true)
            }
        }
    }

    override fun checkEmails(): List<Email> {
        return folders
            .flatMap { folder ->
                doInFolder(folder) {
                    open(Folder.READ_ONLY)
                    search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                        .map { it.toEmail(folder) }
                }
            }
    }

    override fun moveToProcessed(email: Email) {
        with(email.messageId) {
            doInFolder(folder) {
                open(Folder.READ_WRITE)
                // FIXME Ensure msgNum is updated if other emails have been moved before
                val msg = getMessage(msgNum)
                // Ensure the destination folder exists and is open
                val store = this.store
                store.getFolder(processedFolder).use { dest ->
                    if (!dest.exists()) {
                        dest.create(Folder.HOLDS_MESSAGES)
                    }
                    dest.open(Folder.READ_WRITE)
                    // Copy and delete original
                    copyMessages(arrayOf(msg), dest)
                    msg.setFlag(Flags.Flag.DELETED, true)
                    expunge()
                }
            }
        }
    }

    private fun <T> doInFolder(folder: String, block: Folder.() -> T): T {
        val session = Session.getInstance(props, null)
        return session.getStore(config.protocol).use { store ->
            store.connectWith(config)
            store.getFolder(folder).use { f ->
                f.block()
            }
        }
    }

    companion object {
        private fun Store.connectWith(config: EmailServerConfiguration) {
            connect(config.host, config.port, config.user, config.password)
        }
    }
}
