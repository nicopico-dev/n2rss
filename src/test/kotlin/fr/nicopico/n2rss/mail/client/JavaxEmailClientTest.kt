package fr.nicopico.n2rss.mail.client

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class JavaxEmailClientTest {

    companion object {
        private val serverSetup = ServerSetup.IMAP
            .dynamicPort()
            .verbose(true)

        @JvmField
        @RegisterExtension
        val greenMail = GreenMailExtension(serverSetup)
    }

    private lateinit var emailClient: EmailClient

    @BeforeEach
    fun setUp() {
        val user = greenMail.setUser("user@email", "secret password")

        emailClient = JavaxEmailClient(
            protocol = serverSetup.protocol,
            host = greenMail.imap.bindTo,
            port = greenMail.imap.port,
            user = user.email,
            password = user.password,
            inboxFolder = "inbox",
        )
    }

    private fun deliverMessage(from: String, subject: String, content: String) {
        val user = greenMail.userManager.listUser().first()
        val message = GreenMailUtil.createTextEmail(
            user.email,
            from,
            subject,
            content,
            serverSetup
        )
        user.deliver(message)
    }

    @Test
    fun `emailClient should retrieve a list of unread emails from the inbox`() {
        // GIVEN
        deliverMessage(
            from = "from@email.com",
            subject = "Subject",
            content  = "Hello World!"
        )
        deliverMessage(
            from = "from@another-email.com",
            subject = "Subject 2",
            content  = "Hello World! 2"
        )

        // WHEN
        val emails = emailClient.checkEmails()

        // THEN
        emails shouldNot beEmpty()
        emails shouldHaveSize 2
        assertSoftly(emails[0]) {
            it.sender.email shouldBe "from@email.com"
            it.subject shouldBe "Subject"
            it.content shouldBe "Hello World!"
        }

        assertSoftly(emails[1]) {
            it.sender.email shouldBe "from@another-email.com"
            it.subject shouldBe "Subject 2"
            it.content shouldBe "Hello World! 2"
        }
    }

    @Test
    fun `emailClient should mark message as read`() {
        // GIVEN
        deliverMessage(
            from = "from@email.com",
            subject = "Subject",
            content  = "Hello World!"
        )
        deliverMessage(
            from = "from@another-email.com",
            subject = "Subject 2",
            content  = "Hello World! 2"
        )

        // WHEN - THEN
        val emails = emailClient.checkEmails()
        shouldNotThrowAny {
            emailClient.markAsRead(emails[1])
        }
    }
}
