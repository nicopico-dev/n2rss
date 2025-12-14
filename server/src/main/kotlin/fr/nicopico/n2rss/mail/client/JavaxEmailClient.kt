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
                    getFolder(folderName)
                        .use(Folder.READ_ONLY) { folder ->
                            folder
                                .search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                                .map { it.toEmail() }
                        }
                }
            }
    }

    override fun markAsRead(email: Email) {
        val message = email.message
        doInStore {
            message.folder.use(Folder.READ_WRITE) {
                message.setFlag(Flags.Flag.SEEN, true)
            }
        }
    }

    override fun moveToProcessed(email: Email) {
        val message = email.message

        doInStore {
            getFolder(processedFolder).use(
                Folder.READ_ONLY,
                // Ensure the destination folder is present
                createAutomatically = true
            ) { destination ->
                message.folder.use(Folder.READ_WRITE, expungeOnClose = true) { source ->
                    // Copy from `source` to `destination`
                    source.copyMessages(arrayOf(message), destination)

                    // Delete the original
                    message.setFlag(Flags.Flag.DELETED, true)
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

    private fun <T> Folder.use(
        mode: Int,
        expungeOnClose: Boolean = false,
        createAutomatically: Boolean = false,
        block: (Folder) -> T,
    ): T {
        if (isOpen) error("Folder $this is already open")

        if (createAutomatically && !exists()) {
            // This folder can hold folders and messages
            create(Folder.HOLDS_FOLDERS or Folder.HOLDS_MESSAGES)
        }
        open(mode)
        try {
            return block(this)
        } finally {
            close(expungeOnClose)
        }
    }

    companion object {
        private fun Store.connectWith(config: EmailServerConfiguration) {
            connect(config.host, config.port, config.user, config.password)
        }
    }
}
