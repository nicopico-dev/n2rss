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
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.UIDFolder
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
                        LOG.warn("Failed to create processed folder {}", processedFolderName)
                        null
                    } else folder
                } else folder
            } catch (e: MessagingException) {
                LOG.warn("Error while checking/creating processed folder {}", processedFolderName, e)
                null
            }
        }

    init {
        @Suppress("TooGenericExceptionCaught")
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
        val currentMessage = message
        val currentFolder = currentMessage.folder

        val isPotentiallyValid = currentFolder.isOpen
            && !currentMessage.isExpunged
            && currentMessage.messageNumber > 0

        val refetch: () -> Message = {
            val sessionFolder = getSessionFolderByName(currentFolder.fullName)
            sessionFolder.fetchMessage(msgUid, currentMessage.messageNumber)
        }

        return when {
            !isPotentiallyValid -> refetch()
            msgUid == null || currentFolder !is UIDFolder -> currentMessage // Cannot check, optimistic approach
            currentFolder.getUID(currentMessage) == msgUid -> currentMessage // Still valid
            else -> refetch()
        }
    }

    private fun getSessionFolderByName(folderFullName: String): Folder {
        return folders.find { it.fullName == folderFullName }
            ?: processedFolder?.takeIf { it.fullName == folderFullName }
            ?: error("Folder $folderFullName not found in current session")
    }

    private fun Folder.fetchMessage(msgUid: Long?, msgNum: Int): Message {
        return if (msgUid != null && this is UIDFolder) {
            LOG.info("Re-fetching message with UID {} from folder {}", msgUid, this.fullName)
            this.getMessageByUID(msgUid)
                ?: error("Message with UID $msgUid no longer exists in ${this.fullName}")
        } else {
            LOG.warn(
                "Re-fetching message by number {} (unreliable) from folder {}",
                msgNum,
                this.fullName,
            )
            this.getMessage(msgNum)
        }
    }

    @Suppress("LoggingSimilarMessage")
    override fun close() {
        folders.forEach {
            try {
                it.close(/* expunge = */ false)
            } catch (_: IllegalStateException) {
                // Do nothing
            } catch (e: MessagingException) {
                LOG.warn("Failed to close folder {}", it.fullName, e)
            }
        }

        if (processedFolder != null) {
            try {
                processedFolder.close(/* expunge = */ false)
            } catch (_: IllegalStateException) {
                // Do nothing
            } catch (e: MessagingException) {
                LOG.warn("Failed to close folder {}", processedFolder.fullName, e)
            }
        }

        try {
            store.close()
        } catch (e: MessagingException) {
            LOG.warn("Failed to close store", e)
        }
    }
}
