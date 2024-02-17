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
