package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email
import fr.nicopico.n2rss.models.Sender
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.mail.Session
import javax.mail.internet.MimeMessage

class ResourceFileEmailClient(
    private val resFolder: String
) : EmailClient {

    private val mailSession = Session.getDefaultInstance(System.getProperties())

    override fun checkEmails(): List<Email> {
        val loader = javaClass.classLoader
        val resFolderUrl = loader.getResource(resFolder)
            ?: throw IllegalArgumentException("'emails' directory not found.")

        val filePath = Paths.get(resFolderUrl.toURI())
        return Files.walk(filePath)
            .filter { p: Path -> p.toString().endsWith(".eml") }
            .map { emlFilePath ->
                val message = parseEmlFileToMimeMessage(emlFilePath.toString())

                val senderAddress = message.from?.firstOrNull()
                val sender = Sender(senderAddress?.toString() ?: "unknown@unknown.com")
                val subject = message.subject
                val content = message.content.toString().trim()

                Email(sender, subject, content)
            }
            .collect(Collectors.toList())
    }

    private fun parseEmlFileToMimeMessage(filePath: String): MimeMessage {
        return FileInputStream(filePath).use { inputStream ->
            MimeMessage(mailSession, inputStream)
        }
    }
}
