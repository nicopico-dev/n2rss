package fr.nicopico.n2rss.mail.client

import fr.nicopico.n2rss.models.Email
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class ResourceFileEmailClient(
    private val resFolder: String
) : EmailClient {

    private val mailSession = Session.getDefaultInstance(System.getProperties())
    private val readEmails = mutableSetOf<Email>()

    override fun markAsRead(email: Email) {
        readEmails.add(email)
    }

    override fun checkEmails(): List<Email> {
        val loader = javaClass.classLoader
        val resFolderUrl = loader.getResource(resFolder)
            ?: throw IllegalArgumentException("'emails' directory not found.")

        val filePath = Paths.get(resFolderUrl.toURI())
        return Files.walk(filePath)
            .filter { p: Path -> p.toString().endsWith(".eml") }
            .map { emlFilePath ->
                val message = parseEmlFileToMimeMessage(emlFilePath.toString())

                message.toEmail()
            }
            .filter { it !in readEmails }
            .collect(Collectors.toList())
    }

    private fun parseEmlFileToMimeMessage(filePath: String): MimeMessage {
        return FileInputStream(filePath).use { inputStream ->
            MimeMessage(mailSession, inputStream)
        }
    }
}
