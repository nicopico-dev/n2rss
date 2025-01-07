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
package fr.nicopico.n2rss.newsletter.service

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

class ReCaptchaServiceTest {

    private val server = MockWebServer()
    private lateinit var reCaptchaService: ReCaptchaService

    @BeforeEach
    fun setup() {
        server.start()
        // Create a new RestClient with server url
        val restClient = RestClient.create(server.url("/").toString())
        reCaptchaService = ReCaptchaService(restClient)
    }

    @AfterEach
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `isCaptchaValid should return true if response success is true`() {
        // GIVEN
        val responseBody = """{
            |  "success": true,
            |  "challenge_ts": "2024-03-21T21:00:00ZZ",
            |  "hostname": "localhost"
            |}
            |""".trimMargin()

        // enqueue responses
        server.enqueue(
            MockResponse()
                .setBody(responseBody)
                .addHeader(
                    name = "Content-Type",
                    value = "application/json",
                )
        )

        // WHEN
        val isCaptchaValid = reCaptchaService.isCaptchaValid(
            captchaSecretKey = "captchaSecretKey",
            captchaResponse = "captchaResponse",
        )

        // THEN
        isCaptchaValid shouldBe true
        server.takeRequest() should {
            it.requestLine shouldBe "POST /siteverify HTTP/1.1"
            it.body.readUtf8() shouldBe "secret=captchaSecretKey&response=captchaResponse"
        }
    }

    @Test
    fun `isCaptchaValid should return false if response success is false`() {
        // GIVEN
        val responseBody = """{
            |  "success": false,
            |  "challenge_ts": "2024-03-21T21:00:00ZZ",
            |  "hostname": "localhost",
            |  "error-codes": ["missing-input-secret"]
            |}
            |""".trimMargin()

        // enqueue responses
        server.enqueue(
            MockResponse()
                .setBody(responseBody)
                .addHeader(
                    name = "Content-Type",
                    value = "application/json",
                )
        )

        // WHEN
        val isCaptchaValid = reCaptchaService.isCaptchaValid(
            captchaSecretKey = "captchaSecretKey",
            captchaResponse = "captchaResponse",
        )

        // THEN
        isCaptchaValid shouldBe false
    }
}
