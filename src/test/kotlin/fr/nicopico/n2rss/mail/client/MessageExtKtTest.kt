package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Sender
import fr.nicopico.n2rss.utils.toKotlinLocaleDate
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*
import javax.mail.Address
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.InternetHeaders
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import kotlin.random.Random

class MessageExtKtTest {

    @Test
    fun `Message should be converted to Email`() {
        // GIVEN
        val sender = "test@email.com"
        val subject = "Something"
        val content = "Content"
        val messageNumber = 42
        val sentDate = Date(1000000000)
        val receivedDate = Date(1708192826)
        val message = prepareMessage(
            subject = subject,
            content = content,
            from = arrayOf(InternetAddress(sender)),
            messageNumber = messageNumber,
            sentDate = sentDate,
            receivedDate = receivedDate,
        )

        // WHEN
        val email = message.toEmail()

        // THEN
        assertSoftly(email) {
            it.subject shouldBe subject
            it.content shouldBe content
            it.sender shouldBe Sender(sender)
            it.msgnum shouldBe messageNumber
            it.date shouldBe sentDate.toKotlinLocaleDate()
        }
    }

    @Test
    fun `Only the first sender of the Message will be mapped to the Email`() {
        // GIVEN
        val senders = arrayOf<String>("senderA", "senderB", "senderC")
        val message = prepareMessage(
            subject = "Subject",
            content = "Content",
            from = senders
                .map(::InternetAddress)
                .toTypedArray(),
        )

        // WHEN
        val email = message.toEmail()

        // THEN
        email.sender.email shouldBe senders[0]
    }

    @Test
    fun `Email should use the HTML content retrieved from the Message`() {
        // GIVEN
        val htmlContent = "<html>Content</html>"
        val message = prepareMessage(
            subject = "Subject",
            content = MimeMultipart(
                MimeBodyPart(
                    InternetHeaders().apply {
                        addHeader("Content-Type", "text/plain")
                    },
                    Random.nextBytes(20),
                ),
                MimeBodyPart(
                    InternetHeaders().apply {
                        addHeader("Content-Type", "text/html")
                    },
                    htmlContent.toByteArray(),
                ),
            ),
            from = arrayOf(InternetAddress("email@address.com")),
        )

        // WHEN
        val email = message.toEmail()

        // THEN
        email.content shouldBe htmlContent
    }

    @Test
    fun `Only the first HTML part will be used if multiples are present in the Message`() {
        // GIVEN
        val htmlContent1 = "Some magnificent HTML content"
        val htmlContent2 = "Some less interesting HTML content"
        val message = prepareMessage(
            subject = "Subject",
            content = MimeMultipart(
                MimeBodyPart(
                    InternetHeaders().apply {
                        addHeader("Content-Type", "text/html")
                    },
                    htmlContent1.toByteArray(),
                ),
                MimeBodyPart(
                    InternetHeaders().apply {
                        addHeader("Content-Type", "text/html")
                    },
                    htmlContent2.toByteArray(),
                ),
            ),
            from = arrayOf(InternetAddress("email@address.com")),
        )

        // WHEN
        val email = message.toEmail()

        // THEN
        email.content shouldBe htmlContent1
    }

    @Test
    fun `An error should be thrown if the Message do not have any HTML content part`() {
        // GIVEN
        val message = prepareMessage(
            subject = "Subject",
            content = MimeMultipart(
                MimeBodyPart(
                    InternetHeaders().apply {
                        addHeader("Content-Type", "text/plain")
                    },
                    Random.nextBytes(40),
                ),
                MimeBodyPart(
                    InternetHeaders().apply {
                        addHeader("Content-Type", "application/pdf")
                    },
                    Random.nextBytes(240),
                ),
            ),
            from = arrayOf(InternetAddress("email@address.com")),
        )

        // WHEN - THEN
        shouldThrowWithMessage<NoSuchElementException>("no text/html part found in the Message") {
            message.toEmail()
        }
    }

    //region Utils
    private fun prepareMessage(
        subject: String,
        content: Any,
        from: Array<Address>,
        messageNumber: Int = 0,
        sentDate: Date = Date(),
        receivedDate: Date? = null,
    ): Message {
        return mockk<Message> {
            val msg = this
            every { msg.messageNumber } returns messageNumber
            every { msg.subject } returns subject
            every { msg.content } returns content
            every { msg.from } returns from
            every { msg.sentDate } returns sentDate
            every { msg.receivedDate } returns receivedDate
        }
    }
    //endregion
}
