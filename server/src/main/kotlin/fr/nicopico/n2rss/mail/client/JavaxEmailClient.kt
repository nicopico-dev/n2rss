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

    private val storeProps = Properties().apply {
        setProperty("mail.store.protocol", config.protocol)
    }

    override fun checkEmails(): List<Email> {
        return folders
            .flatMap { folderName ->
                doInStore {
                    getFolder(folderName).use { folder ->
                        folder.open(Folder.READ_ONLY)
                        folder
                            .search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                            .map { it.toEmail() }
                    }
                }
            }
    }

    override fun markAsRead(email: Email) {
        val message = email.messageId.message
        doInStore {
            message.folder.open(Folder.READ_ONLY)
            message.setFlag(Flags.Flag.SEEN, true)
        }
    }

    override fun moveToProcessed(email: Email) {
        val message = email.messageId.message

        doInStore {
            getFolder(processedFolder).use { destination ->
                // Ensure the destination folder is present and open
                if (!destination.exists()) {
                    destination.create(Folder.HOLDS_FOLDERS or Folder.HOLDS_MESSAGES)
                }
                destination.open(Folder.READ_ONLY)

                message.folder.use { source ->
                    source.open(Folder.READ_WRITE)

                    // Copy from `source` to `destination`
                    source.copyMessages(arrayOf(message), destination)

                    // Delete the original
                    message.setFlag(Flags.Flag.DELETED, true)
                    source.expunge()
                }
            }
        }
    }

    private fun <T> doInStore(block: Store.() -> T): T {
        val session = Session.getInstance(storeProps, null)
        return session.getStore(config.protocol).use { store ->
            store.connectWith(config)
            store.block()
        }
    }

    companion object {
        private fun Store.connectWith(config: EmailServerConfiguration) {
            connect(config.host, config.port, config.user, config.password)
        }
    }
}
