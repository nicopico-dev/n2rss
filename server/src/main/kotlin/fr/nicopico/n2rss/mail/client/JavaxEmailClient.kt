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
import jakarta.mail.FolderNotFoundException
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.search.FlagTerm
import org.slf4j.LoggerFactory
import java.util.Properties

private val LOG = LoggerFactory.getLogger(JavaxEmailClient::class.java)

class JavaxEmailClient(
    private val config: EmailServerConfiguration,
    private val folders: List<String>,
    private val processedFolder: String = "Trash",
) : EmailClient {

    private val storeProps = Properties().apply {
        setProperty("mail.store.protocol", config.protocol)
    }

    override fun openSession(): EmailClientSession {
        val session = Session.getInstance(storeProps, null)
        val store = session.getStore(config.protocol).also {
            it.connectWith(config)
        }
        return JavaxEmailClientSession(
            store = store,
            folderNames = folders,
            processedFolderName = processedFolder,
        )
    }

    @Deprecated("Use EmailClientSession with openSession() instead")
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

    @Deprecated("Use EmailClientSession with openSession() instead")
    override fun markAsRead(email: Email) {
        val message = email.message
        doInStore {
            message.folder.use(Folder.READ_WRITE) { folder ->
                val freshMessage = if (message.isExpunged || !folder.isOpen || message.messageNumber <= 0) {
                    folder.getMessage(message.messageNumber)
                } else {
                    message
                }
                freshMessage.setFlag(Flags.Flag.SEEN, true)
            }
        }
    }

    @Deprecated("Use EmailClientSession with openSession() instead")
    override fun moveToProcessed(emails: List<Email>) {
        if (emails.isEmpty()) return
        val srcFolder = emails.first().message.folder

        doInStore {
            getFolder(processedFolder).use(
                Folder.READ_ONLY,
                // Ensure the destination folder is present
                createAutomatically = true
            ) { destination ->
                srcFolder.use(Folder.READ_WRITE, expungeOnClose = true) { folder ->
                    val freshMessages = emails
                        .map { it.message }
                        .map { message ->
                            // Ensure freshness
                            if (message.isExpunged || !folder.isOpen || message.messageNumber <= 0) {
                                folder.getMessage(message.messageNumber)
                            } else {
                                message
                            }
                        }
                        .toTypedArray()

                    // Copy from `folder` to `destination`
                    folder.copyMessages(freshMessages, destination)

                    // Delete the originals
                    freshMessages.forEach { message ->
                        message.setFlag(Flags.Flag.DELETED, true)
                    }
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
        if (!exists()) {
            if (createAutomatically) {
                // This folder can hold folders and messages
                create(Folder.HOLDS_FOLDERS or Folder.HOLDS_MESSAGES)
            } else {
                throw FolderNotFoundException(this, "Folder $name does not exist")
            }
        }

        if (isOpen) error("Folder $this is already open")
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

private class JavaxEmailClientSession(
    private val store: Store,
    folderNames: List<String>,
    processedFolderName: String,
) : EmailClientSession {

    private val folders: List<Folder> = folderNames
        .map { folderName ->
            store.getFolder(folderName)
        }
        .filter { folder ->
            if (!folder.exists()) {
                LOG.warn("Folder {} does not exist, skipping", folder.name)
                false
            } else true
        }

    private val processedFolder: Folder? = store.getFolder(processedFolderName)
        .let { folder ->
            try {
                if (!folder.exists()) {
                    // Ensure the folder exists
                    LOG.info("Folder {} does not exist, creating it", processedFolderName)
                    val created = folder.create(Folder.HOLDS_FOLDERS or Folder.HOLDS_MESSAGES)
                    if (!created) {
                        LOG.error("Failed to create folder {}", processedFolderName)
                        null
                    } else folder
                } else folder
            } catch (e: Exception) {
                LOG.error("Error while checking/creating processed folder {}", processedFolderName, e)
                null
            }
        }

    init {
        try {
            folders.forEach { folder ->
                folder.open(Folder.READ_WRITE)
            }
            processedFolder?.open(Folder.READ_WRITE)
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    override fun checkEmails(): List<Email> {
        return folders.flatMap { folder ->
            folder
                .search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                .map { it.toEmail() }
        }
    }

    override fun markAsRead(email: Email) {
        val freshMessage = email.getFreshMessage()
        freshMessage.setFlag(Flags.Flag.SEEN, true)
    }

    override fun moveToProcessed(emails: List<Email>) {
        val destination = processedFolder
        if (destination == null) {
            LOG.warn("Processed folder is not available, cannot move emails")
            return
        }
        val messagesByFolder = emails
            .map { it.getFreshMessage() }
            .groupBy { it.folder }

        messagesByFolder.forEach { (folder, messages) ->
            // Copy all messages to the destination, then delete the source messages
            folder.copyMessages(messages.toTypedArray<Message>(), destination)
            messages.forEach { message ->
                message.setFlag(Flags.Flag.DELETED, true)
            }
            folder.expunge()
        }
    }

    private fun Email.getFreshMessage(): Message {
        val folder = message.folder
        return if (message.isExpunged || !folder.isOpen || message.messageNumber <= 0) {
            LOG.info(
                "Message is stale (expunged: {}, open: {}, number: {}), re-fetching",
                message.isExpunged, folder.isOpen, message.messageNumber
            )
            folder.getMessage(message.messageNumber)
        } else {
            message
        }
    }

    override fun close() {
        folders.forEach {
            try {
                it.close(/* expunge = */ false)
            } catch (_: IllegalStateException) {
                // Do nothing
            }
        }
        try {
            processedFolder?.close(/* expunge = */ false)
        } catch (_: IllegalStateException) {
            // Do nothing
        }
        store.close()
    }
}
