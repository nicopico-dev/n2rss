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
package fr.nicopico.n2rss.controller.home

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.controller.Url
import fr.nicopico.n2rss.service.NewsletterService
import fr.nicopico.n2rss.service.ReCaptchaService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import java.net.URL

@Validated
@Controller
class HomeController(
    private val newsletterService: NewsletterService,
    private val reCaptchaService: ReCaptchaService,
    private val properties: N2RssProperties,
) {

    @GetMapping("/")
    fun home(request: HttpServletRequest, model: Model): String {
        val newslettersInfo = newsletterService.getNewslettersInfo()
            .filter { it.publicationCount > 0 }
        val requestUrl: String = request.requestURL
            .let {
                if (properties.feeds.forceHttps) {
                    it.replace(Regex("http://"), "https://")
                } else {
                    it.toString()
                }
            }

        with(model) {
            addAttribute("newsletters", newslettersInfo)
            addAttribute("requestUrl", requestUrl)
            addAttribute("reCaptchaSiteKey", properties.recaptcha.siteKey)
        }
        return "index"
    }

    @PostMapping("/send-request")
    fun requestNewsletter(
        @NotEmpty @Url @RequestParam("newsletterUrl") newsletterUrl: String,
        @RequestParam("g-recaptcha-response") captchaResponse: String,
        response: HttpServletResponse,
    ) {
        // TODO Return success or error messages to the page
        val isCaptchaValid = reCaptchaService.isCaptchaValid(
            captchaSecretKey = properties.recaptcha.secretKey,
            captchaResponse = captchaResponse,
        )

        if (isCaptchaValid) {
            newsletterService.saveRequest(URL(newsletterUrl))
            response.sendRedirect("/")
        } else {
            response.sendError(
                HttpStatus.BAD_REQUEST.value(),
                "reCaptcha challenge failed",
            )
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleExceptions(
        exception: ConstraintViolationException,
    ): ResponseEntity<Map<String, String?>> {
        val errors = exception.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }
        return ResponseEntity.badRequest().body(errors)
    }
}
