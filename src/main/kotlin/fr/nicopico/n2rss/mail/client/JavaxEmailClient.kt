package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email
import java.util.*
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.search.FlagTerm

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

    override fun checkEmails(): List<Email> {
        val session = Session.getInstance(props, null)
        session.getStore(protocol).use { store ->
            store.connect(host, port, user, password)

            store.getFolder(inboxFolder).use { inbox ->
                inbox.open(Folder.READ_ONLY)

                return inbox
                    .search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                    .map(Message::toEmail)
            }
        }
    }
}
