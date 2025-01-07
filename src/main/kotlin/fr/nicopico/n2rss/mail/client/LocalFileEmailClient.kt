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
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class LocalFileEmailClient(
    private val emailFolder: String
) : EmailClient {

    private val mailSession = Session.getDefaultInstance(System.getProperties())
    private val readEmails = mutableSetOf<Email>()

    override fun markAsRead(email: Email) {
        readEmails.add(email)
    }

    override fun checkEmails(): List<Email> {
        val filePath = Paths.get(emailFolder)
        require(filePath.toFile().exists()) {
            "$filePath does not exist"
        }

        return Files.walk(filePath)
            .filter { p: Path -> p.toString().endsWith(".eml") }
            .map { emlFilePath ->
                val message = parseEmlFileToMimeMessage(emlFilePath.toString())

                message.toEmail(emailFolder)
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
