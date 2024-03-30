/*
 * Copyright (c) 2024 Nicolas PICON
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
package fr.nicopico.n2rss.service

import com.jayway.jsonpath.JsonPath
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

private val LOG = LoggerFactory.getLogger(ReCaptchaService::class.java)

@Service
class ReCaptchaService {

    fun isCaptchaValid(
        captchaSecretKey: String,
        captchaResponse: String
    ): Boolean {
        val url = "https://www.google.com/recaptcha/api/siteverify"

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("secret", captchaSecretKey)
        params.add("response", captchaResponse)

        val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity(params, HttpHeaders())

        // RestTemplate() is deprecated in Spring 5. You may use WebClient instead.
        val restTemplate = RestTemplate()
        val response: ResponseEntity<String> = restTemplate.postForEntity(url, request, String::class.java)

        return JsonPath
            .parse(response.body)
            .read("""["success"]""")
    }
}
